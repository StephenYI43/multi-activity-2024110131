param(
    [string]$Workspace = 'C:\Users\48604\Documents\ChatGPT\安卓开发'
)
$ErrorActionPreference = 'Stop'
$project = Join-Path $Workspace 'assignment3-multi-activity'
$assets = Join-Path $project 'report-assets'
$reference = Join-Path $Workspace '实验三+多Activity.docx'
$output = Join-Path $Workspace '实验三_多Activity_实验报告_2024110131宋珺熠.docx'
$qa = Join-Path $Workspace 'tmp\assignment3'
$repoUrl = 'https://github.com/StephenYI43/multi-activity-2024110131'
$wps = New-Object -ComObject kwps.Application
$wps.Visible = $false
$wps.DisplayAlerts = 0
$doc = $wps.Documents.Open($reference, $false, $false)
$doc.SaveAs($output, 12)

function Set-Text([int]$Index, [string]$Text) {
    $p = $doc.Paragraphs.Item($Index).Range
    $p.Text = $Text + "`r"
}
function Add-Text([string]$Text, [double]$Size = 10.5, [bool]$Bold = $false) {
    $start = $doc.Content.End - 1
    $r = $doc.Range($start, $start)
    $r.InsertBefore($Text + "`r")
    $r = $doc.Range($start, $start + $Text.Length + 1)
    $r.Font.Name = 'Times New Roman'
    $r.Font.NameFarEast = '宋体'
    $r.Font.Size = $Size
    $r.Font.Bold = [int]$Bold
    $r.Font.Color = 0
    $r.Font.Underline = 0
    $r.ParagraphFormat.Alignment = 0
    $r.ParagraphFormat.LeftIndent = 0
    $r.ParagraphFormat.RightIndent = 0
    $r.ParagraphFormat.FirstLineIndent = 0
    $r.ParagraphFormat.SpaceBefore = 0
    $r.ParagraphFormat.SpaceAfter = 6
    $r.ParagraphFormat.LineSpacingRule = 0
    $r.ParagraphFormat.PageBreakBefore = 0
    $r.ParagraphFormat.KeepWithNext = 0
    $r.ParagraphFormat.KeepTogether = 0
    $r.ParagraphFormat.Borders.Enable = 0
    $r.ListFormat.RemoveNumbers()
    return $r
}
function Heading([string]$Text) {
    $r = Add-Text $Text 12 $true
    $r.ParagraphFormat.KeepWithNext = -1
    return $r
}
function Page([string]$Text) {
    $r = Heading $Text
    $r.ParagraphFormat.PageBreakBefore = -1
}
function Images([string[]]$Names, [double]$Width) {
    $start = $doc.Content.End - 1
    $r = Add-Text '' 10.5
    $r.ParagraphFormat.Alignment = 1
    foreach ($name in $Names) {
        $pos = $doc.Content.End - 1
        $insert = $doc.Range($pos, $pos)
        $shape = $doc.InlineShapes.AddPicture((Join-Path $assets $name), $false, $true, $insert)
        $shape.LockAspectRatio = -1
        $shape.Width = $Width
        $shape.AlternativeText = $name
        $doc.Range($shape.Range.End, $shape.Range.End).InsertAfter(' ')
    }
    $doc.Range($doc.Content.End - 1, $doc.Content.End - 1).InsertAfter("`r")
}

# Fill the retained source slots before adding continuation pages.
$find = $doc.Paragraphs.Item(1).Range.Find
$null = $find.Execute('年  月  日', $false, $false, $false, $false, $false, $true, 0, $false, '2026年10月13日', 2)
$dateRange = $doc.Paragraphs.Item(1).Range
if ($dateRange.Find.Execute('2026年10月13日')) { $dateRange.Font.Size = 8 }
Set-Text 2 '计算机科学学院   2024级1班   实验名称：多Activity'
Set-Text 3 '姓名：宋珺熠   学号：2024110131   指导老师：李贵洋   实验成绩：_____'
Set-Text 6 ('GitHub网址：' + $repoUrl)
foreach ($index in @(28,29,30)) {
    $r = $doc.Paragraphs.Item($index).Range
    $r.Text = $r.Text -replace '(?i)gitee', 'GitHub'
}
$gradeNote = $doc.Paragraphs.Item(68).Range.Text.Trim()
$tailStart = $doc.Paragraphs.Item(34).Range.Start
$doc.Range($tailStart, $doc.Content.End - 1).Delete()
$doc.Paragraphs.Item(33).Range.ParagraphFormat.PageBreakBefore = -1
$doc.Paragraphs.Item(33).Range.ParagraphFormat.KeepWithNext = -1

Heading '1 程序结构与传参结果' | Out-Null
Add-Text '本实验完成了一个主 Activity 和四个二级 Activity。MainActivity 提供四个功能入口；ReceiveActivity 显示传入消息；ResultActivity 输入并返回内容；CameraActivity 调用系统相机；StopwatchActivity 实现连续计时。各页面使用统一的深色界面，应用标题为宋珺熠2024110131。' | Out-Null
Add-Text '实验环境为 Windows 11、Android Studio、内置 JBR 21、Android SDK 35、Gradle 8.13、Android Gradle Plugin 8.13.2 和 Kotlin 2.1.20。在 Android API 35 模拟器上安装 APK 并验证以下过程。' | Out-Null
Images @('01-main.png','02-forward.png','04-returned.png') 125
Add-Text '图1至图3依次为主页面、正向传参页面和返回结果页面。默认消息包含姓名与学号，二级页面能正确接收。第二个页面输入 Activity test passed 2024110131 后，主页面显示返回成功；旋转后返回结果保持不变。空输入会提示错误，取消返回会保留上一次结果。' | Out-Null

Page '2 系统相机拍照与照片恢复'
Add-Text '点击第三个入口后进入 CameraActivity，再点击打开相机拍照。程序使用 MediaStore.ACTION_IMAGE_CAPTURE 隐式 Intent，将私有照片文件对应的 content URI 放入 EXTRA_OUTPUT，并临时授予相机读写权限。拍摄确认后，页面读取原图并按比例预览。' | Out-Null
Images @('05-camera-ready.png','06-photo-result.png') 135
Add-Text '图4和图5为拍照前与拍照成功后的真实运行界面。本次模拟器相机生成原图1392×1856像素，文件约73 KB。照片中的像素图案来自模拟器相机的模拟场景。' | Out-Null
Images @('07-photo-landscape.png') 378
Add-Text '图6为旋转后的照片预览。Bundle 保存已显示照片和待拍摄文件的名称，重建页面后从应用私有文件恢复照片。取消重拍后，原照片及尺寸信息仍然保留。' | Out-Null

Page '3 秒表横竖屏连续计时'
Add-Text '秒表保存累计时间 accumulated、开始时刻 startedAt 和运行标志 running。当前读数由 SystemClock.elapsedRealtime() 的差值计算，Handler 只负责刷新界面。onPause 停止刷新回调，运行中的秒表仍继续计算经过时间。' | Out-Null
Images @('08-timer-portrait.png') 117
Add-Text '图7为竖屏运行状态。第一次暂停读取到2.47秒，继续运行后旋转为横屏，再读取到4.41秒；恢复竖屏后为5.97秒。横竖屏切换没有归零或重复累计。' | Out-Null
Images @('09-timer-landscape.png') 378
Add-Text '图8为运行中切换到横屏的界面。另行验证了暂停后读数不变、暂停状态下旋转保持读数、继续后计时增加，以及复位后归零。秒表也支持计次记录，接入物理键盘时可用空格键开始或暂停。' | Out-Null

Page '4 传参与返回结果的主要代码'
Add-Text '以下图片是项目实际 Kotlin 源码的带行号摘录。主页面通过显式 Intent 指定目标 Activity，并用 putExtra 传递消息；返回页面通过 setResult 设置 RESULT_OK 和返回 Intent，然后 finish 结束页面；主页面在 onActivityResult 中更新结果。' | Out-Null
Images @('code-01-intents.png') 405
Add-Text '取消操作不会覆盖已有结果。输入框使用稳定的资源 ID，由 Android 保存视图状态；主页面另在 onSaveInstanceState 中保存 lastResult，确保旋转后仍能显示。' | Out-Null

Page '5 隐式相机调用的主要代码'
Add-Text '相机 Intent 不指定组件名，由系统选择可以响应拍照动作的应用。PhotoProvider 不对外导出，只允许访问应用私有 photos 目录中的指定 JPEG 文件；相机结束后撤销临时 URI 授权。应用将拍照交给系统相机，因此不直接申请 CAMERA 或外部存储权限。' | Out-Null
Images @('code-02-camera.png') 405
Add-Text '预览时先读取图片边界，再按尺寸采样解码，并处理 JPEG 的旋转信息，避免直接加载大图造成过多内存占用。取消拍摄会清理本次空文件，成功重拍后会替换旧文件。Bundle 中只保存文件名，不保存大 Bitmap。' | Out-Null

Page '6 秒表状态保存与生命周期'
Add-Text '屏幕旋转时没有锁定方向，也没有用 configChanges 绕过重建。StopwatchActivity 的 onCreate 从 Bundle 恢复计时状态，onSaveInstanceState 保存运行标志、累计时间、开始时刻和计次数据。恢复原始开始时刻后，时间差仍覆盖页面重建期间的时间。' | Out-Null
Images @('code-03-stopwatch.png') 405
Add-Text 'LabActivity 在 onCreate、onStart、onResume、onPause、onStop、onDestroy 和 onSaveInstanceState 中输出 ActivityLifecycle 日志。模拟器实测可观察到旋转时旧页面暂停、保存、停止和销毁，以及新页面创建、启动和恢复的过程。运行日志保存在项目 report-assets/lifecycle-log.txt 中。' | Out-Null
Add-Text '界面标题、按钮、提示、默认消息和时间格式均从 res/values/strings.xml 读取。Kotlin 中保留的 Intent extra 名、Bundle key、日志标签和 MIME 类型属于技术协议标识，不是界面文案。用户输入内容按原样显示。' | Out-Null

Page '四 实验结果的分析与评价'
Heading '1 功能验证结果' | Out-Null
Add-Text '正向传参、传参页面旋转恢复、返回内容、主页面返回结果旋转恢复、空输入拦截和取消返回保留旧结果均通过。系统相机能够打开、拍摄并返回完整照片，旋转恢复和取消重拍保留旧照片也通过。' | Out-Null
Add-Text '秒表运行中横竖屏切换的读数依次为2.47秒、4.41秒和5.97秒。暂停后等待和旋转均保持读数，继续计时后读数增加，复位后归零。Gradle assembleDebug 构建成功，Android Lint 检查为0个错误、5个警告；警告包括 SDK 版本更新建议、框架 EXIF 实现和备份规则建议，不影响本次 API 35 实验验证。' | Out-Null
Heading '2 问题分析与处理' | Out-Null
Add-Text '静态检查最初发现导航栏样式使用了 API 27 才支持的属性，与最低 API 23 不一致，删除该非必要属性后检查通过。自动测试在横屏时仍使用竖屏滚动坐标，导致找不到屏幕外控件，改为根据当前设备尺寸滚动后通过。秒表高频刷新会让 UIAutomator 无法取得空闲界面，因此测试在截图后短暂暂停读取读数，再继续并旋转；运行截图和数值断言共同验证连续计时。' | Out-Null
Heading '3 实验体会与改进方向' | Out-Null
Add-Text '通过本实验，我将 Activity 的启动、传入参数和返回结果串成完整流程，并验证了配置变化时状态的保存与恢复。秒表需要保存计时依据，而不能只保存屏幕上的数字；照片需要保存文件位置，而不能把大图片直接放进 Bundle。实际验证说明这些状态在横竖屏变化后能够恢复。' | Out-Null
Add-Text '当前结果来自 API 35 模拟器。不同品牌真机的相机返回行为以及更多 Android 版本尚需进一步兼容性测试；未来可使用 AndroidX Activity Result API、FileProvider 和新版 ExifInterface，并将图片解码放到后台线程。' | Out-Null
Heading '4 项目与参考资料' | Out-Null
Add-Text ('GitHub项目地址：' + $repoUrl) | Out-Null
Add-Text '主要文件：MainActivity.kt、ReceiveActivity.kt、ResultActivity.kt、CameraActivity.kt、StopwatchActivity.kt、LabActivity.kt、PhotoProvider.kt、AndroidManifest.xml 和 strings.xml。测试脚本与运行截图一并保存在仓库中。' | Out-Null
Add-Text '参考资料：课程附件《采用隐式Intent调用相机拍照功能》；Android 官方拍照文档 https://developer.android.com/media/camera/camera-deprecated/photobasics；Activity 生命周期文档 https://developer.android.com/guide/components/activities/activity-lifecycle。' | Out-Null
Add-Text $gradeNote 9 | Out-Null
$doc.Save()
$doc.ExportAsFixedFormat((Join-Path $qa 'report.pdf'), 17)
[pscustomobject]@{Output=$output; Pages=$doc.ComputeStatistics(2); InlineImages=$doc.InlineShapes.Count}
$doc.Close(0)
[void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc)
[void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($wps)
