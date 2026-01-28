# Capsulyric

> Provides status bar lyrics based on Live Update.
> 提供基于 Live Update 的状态栏歌词。

## 📱 Compatibility (兼容性)

* **Android System**: Android 16+
* **Vendor OS Requirements**:
    * HyperOS 3.0.300+
    * ColorOS 16+

## 🛡️ Privacy & Disclaimer (隐私与免责)

* **0-Hook**: The software itself works without any hooking mechanisms.
* **Offline**: No internet permission required. No data is transmitted.
* **Disclaimer**: The developer assumes no liability for any risks caused by the software.

软件本身 0 hook，没有联网功能，不会传输任何数据，开发者不对软件可能造成的风险承担责任。

---

## ⚙️ Working Modes (工作模式)

### 1. LSPosed Mode (LSPosed 模式)
**Requirement**: **SuperLyric** must be installed and activated via LSPosed.
In this mode, Capsulyric fetches lyrics from any music app supported by SuperLyric.

要求已经安装 **SuperLyric** 并通过 LSPosed 激活，此时软件可以获取到 SuperLyric 支持的音乐软件的歌词。

### 2. Fully 0-Hook Mode (完全 0-hook 模式)
**Supported Apps**:
* QQ Music (`com.tencent.qqmusic`)
* Xiaomi Music (`com.miui.player`)

**Setup**:
1.  Enable **"Car Bluetooth Lyrics"** (车载蓝牙歌词) inside the music app settings.
2.  Grant **Notification Access** to Capsulyric.
3.  *Note: Some newer versions of QQ Music may require a Bluetooth headset connection to function.*

只支持 QQ 音乐和小米音乐，在开启软件内“车载蓝牙歌词”并且开启通知使用权的情况下，软件可以获取到歌词（部分较新版本的 QQ 音乐需要连接蓝牙耳机）。

---

## 🛠️ Build (构建)

Clone the project and run the following command to generate a debug APK:
clone 本项目后使用以下命令即可打出 debug 包：

```bash
./gradlew assembleDebug
```

## 🤝 Credits (致谢)

* [SuperLyric](https://github.com/HChenX/SuperLyric)(GPL-3.0)
* [SuperLyricAPI](https://github.com/HChenX/SuperLyricApi) (LGPL-2.1 Licensed)
