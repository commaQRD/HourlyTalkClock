# 整点语音钟 HourlyTalkClock

免 Root 的 Android 语音报时钟。适合小爱老师工程机 + LOS 17 GSI，也兼容 Android 8.1+。

- 环境光调节音量
- NTP 校时
- 整点提示音 + 中文报时
- 多轮语音对话（时间 / 天气 / 换声音）
- 启动图标默认用 [commaQRD](https://github.com/commaQRD) 的 GitHub 头像

仓库：https://github.com/commaQRD/HourlyTalkClock

## 不用 Android Studio，用 Python 编译

需要本机 JDK 17 和 Python 3。

```bash
git clone https://github.com/commaQRD/HourlyTalkClock.git
cd HourlyTalkClock
pip install -r requirements-build.txt
python3 build_apk.py --github commaQRD
```

APK 输出在 `dist/HourlyTalkClock.apk`。

## TWRP

把编好的 `HourlyTalkClock.apk` 放到刷机 zip 根目录，与 `META-INF` 同级，再在 TWRP 里 Install。

LOS vanilla 没有 Google 语音识别时，对话可能不可用，按钮报时和天气仍可测。
