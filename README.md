> 不同于iOS，Android的webView不支持打开office和pdf文档，所以当我们遇到在应用内打开office和pdf文档的需求时，往往无法从系统原生功能去支持。这篇文章的写下笔者在Android应用中集成office和pdf文件能力的心得，附上demo地址：[https://github.com/windinwork/OfficeApplication](https://github.com/windinwork/OfficeApplication)

### 一、确定解决方案

Android应用打开office和pdf文件。常用的有以下四种解决方案：
1. 在线网页打开文件方案：通过微软或谷歌提供的在线页面打开office和pdf文件
2. 集成相关文档处理开源库：通过集成开源库类似于AndroidPdfViewer
3. 通过系统中的第三方应用打开文档
4. 集成腾讯x5 sdk文件能力

四种方案各有优劣，这里笔者选择了x5 sdk为主要手段，第三方应用辅助的这样一种解决方案

### 二、集成x5内核

腾讯官方提供的x5内核有两个版本，这里选择具有文件能和的sdk：


![](https://user-gold-cdn.xitu.io/2019/1/19/1686501b9e843889?w=1061&h=498&f=png&s=50584)

接下来的集成可以参考[x5内核接入文档](https://x5.tencent.com/tbs/guide/sdkInit.html)，这里便不详述。集成的主要工作便是集成jar包和so文件，并在Application初始化时调用QbSdk.initX5Environment(context, callback)来完成初始化工作。

### 三、集成TbsReader

x5内核中提供了TbsReaderView，让我们可以通过这个类在App中显示文档。考虑到TbsReaderView这个类具有生命周期的方法，我们把它封装在一个Fragment中，方便我们的调用。TbsReaderView的主要方法有两个，一个是preOpen(String, boolean)，另一个是openFile(Bundle)。preOpen(String, boolean)是用来检测x5文件能力是否成功初始化和是否支持打开文件的格式，当符合打开文件的条件时该方法返回true；openFile(Bundle)则是在preOpen(String, boolean)的返回值为true的情况进行调用，顾名思义这个方法是用来打开文件的，其中bundle用来传入文件路径。

```
String path = file.getPath();
String format = parseFormat(path);
boolean preOpen = mReaderView.preOpen(format, false); // 该状态标志x5文件能力是否成功初始化并支持打开文件的格式
if (preOpen) { // 使用x5内核打开office文件
    Bundle bundle = new Bundle();
    bundle.putString("filePath", path);
    bundle.putString("tempPath", StorageUtils.getTempDir(context).getPath());
    mReaderView.openFile(bundle);
}
```

有了这部分核心代码，TbsReaderView基本上就能打开Office和PDF文件了。

![](https://user-gold-cdn.xitu.io/2019/1/23/168788349cf04caa?w=1080&h=2280&f=jpeg&s=152346)

### 四、完善文件能力

市面上的安卓手机各式各样，虽然集成了TbsReaderView，但是还是会收到用户反馈说无法打开Office文件。这是因为用户手机上的x5文件能力没有初始化成功，至于为什么没有初始化成功，原因还无法确定。针对这部分用户，我们需要在他们无法使用TbsReaderView浏览Office文件的情况下，提供另外的途径去打开Office文件。大致思路是检测到TbsReaderView无法打开Office或PDF时，跳转到第三方应用去打开。这里x5的jar包提供了这样一个api:openFileReader(Context, String, HashMap<String, String>, ValueCallback<String>)用来使用第三方应用打开文件，并且支持前往下载具有Office浏览功能的QQ浏览器，这样的功能对用户比较友好，我们可以直接拿来用。


![](https://user-gold-cdn.xitu.io/2019/1/23/168787ebe9b1ff09?w=380&h=611&f=png&s=19921)

然而，x5的jar包中使用第三方应用打开时调用了Uri.fromFile(file)，这个生成文件Uri的方法在Android7.0以下有效，但在Android7.0及以上会造成崩溃，这是Android7.0的文件权限管理导致。为了使Android7.0及以上的用户可以正常跳转到第三方应用打开，我们需要使用FileProvider去获取Uri，但代码在Jar包中写死了。幸运的是，经过多次尝试，发现可以将跳转到第三方应用打开的这部分代码复制出来，修正Uri.fromFile(file)的代码以正常调用，免去了要修改jar的麻烦。这里笔者把这部分代码封装在一个叫TbsReaderAssist的类中，辅助调用。

这样一来，一个比较完善的打开Office和PDF的功能就算做完成。

```
String path = file.getPath();
String format = parseFormat(path);
boolean preOpen = mReaderView.preOpen(format, false); // 该状态标志x5文件能力是否成功初始化并支持打开文件的格式
if (preOpen) { // 使用x5内核打开office文件
    Bundle bundle = new Bundle();
    bundle.putString("filePath", path);
    bundle.putString("tempPath", StorageUtils.getTempDir(context).getPath());
    mReaderView.openFile(bundle);
} else { // 打开文件失败，可能是由于x5内核未成功初始化引起
    if (QbSdk.isSuportOpenFile(format, 1)) { // 再次检查文件是否支持
        HashMap<String, String> params = new HashMap<>();
        params.put("style", "1");
        params.put("local", "false");
        TbsReaderAssist.openFileReader(context, path, params, null);
    }
}
```

### 五、总结

这里笔者写了一个App打开Office或PDF文件的解决方案，个人认为对于一个App来说是相对完善的处理。这里是demo的地址：[https://github.com/windinwork/OfficeApplication](https://github.com/windinwork/OfficeApplication)，共享出来，可以让有需要做类似功能的小伙伴少走些弯路。