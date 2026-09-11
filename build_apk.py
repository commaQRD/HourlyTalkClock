#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
不用 Android Studio，本机用 Python 拉图标 + 命令行 SDK + Gradle 打 APK。

用法：
  pip install -r requirements-build.txt
  python3 build_apk.py --github commaQRD
"""
from __future__ import annotations

import argparse
import os
import platform
import shutil
import stat
import subprocess
import sys
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DIST = ROOT / "dist"
RES = ROOT / "app" / "src" / "main" / "res"
WRAPPER_JAR = ROOT / "gradle" / "wrapper" / "gradle-wrapper.jar"
WRAPPER_URL = (
    "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/"
    "gradle/wrapper/gradle-wrapper.jar"
)
CMDLINE_VER = "11076708"
SDK_BASE = "https://dl.google.com/android/repository"
DEFAULT_GITHUB = "commaQRD"


def log(msg: str) -> None:
    print(f"[build] {msg}", flush=True)


def download(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    log(f"下载 {url}")
    req = urllib.request.Request(url, headers={"User-Agent": "HourlyTalkClock-builder"})
    with urllib.request.urlopen(req, timeout=120) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f)


def fetch_avatar(github: str | None, avatar: str | None) -> Path:
    out = ROOT / "avatar_source.png"
    if avatar:
        shutil.copyfile(avatar, out)
        return out
    user = github or DEFAULT_GITHUB
    urls = [
        f"https://github.com/{user}.png?size=512",
        f"https://avatars.githubusercontent.com/{user}",
    ]
    last_err = None
    for u in urls:
        try:
            download(u, out)
            return out
        except Exception as e:
            last_err = e
    raise RuntimeError(f"头像下载失败: {last_err}")


def make_icons(src_path: Path) -> None:
    from PIL import Image, ImageDraw
    src = Image.open(src_path).convert("RGBA")
    side = min(src.size)
    src = src.crop(((src.width - side) // 2, (src.height - side) // 2, (src.width + side) // 2, (src.height + side) // 2))
    sizes = {"mipmap-mdpi": 48, "mipmap-hdpi": 72, "mipmap-xhdpi": 96, "mipmap-xxhdpi": 144, "mipmap-xxxhdpi": 192}
    for folder, px in sizes.items():
        d = RES / folder
        d.mkdir(parents=True, exist_ok=True)
        im = src.resize((px, px), Image.LANCZOS)
        mask = Image.new("L", (px, px), 0)
        ImageDraw.Draw(mask).ellipse((1, 1, px - 2, px - 2), fill=255)
        out = Image.new("RGBA", (px, px), (0, 0, 0, 0))
        out.paste(im, (0, 0), mask)
        out.save(d / "ic_launcher.png")
        out.save(d / "ic_launcher_round.png")
    log("已写入圆形启动图标（GitHub 头像）")


def ensure_wrapper_jar() -> None:
    if WRAPPER_JAR.exists() and WRAPPER_JAR.stat().st_size > 10000:
        return
    download(WRAPPER_URL, WRAPPER_JAR)


def sdk_root() -> Path:
    env = os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME")
    if env:
        return Path(env)
    return Path.home() / "Android" / "cmdline-sdk"


def host_tag() -> str:
    sysname = platform.system().lower()
    if sysname == "darwin":
        return "mac"
    if sysname == "windows":
        return "win"
    return "linux"


def ensure_sdk(sdk: Path) -> Path:
    sdkmanager = None
    for cand in sdk.rglob("sdkmanager"):
        sdkmanager = cand
        break
    if sdkmanager is None:
        zip_name = f"commandlinetools-{host_tag()}-{CMDLINE_VER}_latest.zip"
        zpath = sdk / zip_name
        download(f"{SDK_BASE}/{zip_name}", zpath)
        with zipfile.ZipFile(zpath) as z:
            z.extractall(sdk / "_tmp")
        dest = sdk / "cmdline-tools" / "latest"
        dest.parent.mkdir(parents=True, exist_ok=True)
        extracted = next((sdk / "_tmp").iterdir())
        if dest.exists():
            shutil.rmtree(dest)
        extracted.rename(dest)
        shutil.rmtree(sdk / "_tmp", ignore_errors=True)
        sdkmanager = dest / "bin" / "sdkmanager"
        if host_tag() == "win":
            sdkmanager = dest / "bin" / "sdkmanager.bat"
    sdkmanager.chmod(sdkmanager.stat().st_mode | stat.S_IEXEC)
    env = os.environ.copy()
    env["ANDROID_SDK_ROOT"] = str(sdk)
    pkgs = ["platform-tools", "platforms;android-34", "build-tools;34.0.0"]
    log("安装 Android 命令行组件")
    subprocess.run([str(sdkmanager), f"--sdk_root={sdk}", "--licenses"], input="y\n" * 80, text=True, env=env, check=False)
    subprocess.check_call([str(sdkmanager), f"--sdk_root={sdk}", *pkgs], env=env)
    (ROOT / "local.properties").write_text(f"sdk.dir={sdk.as_posix()}\n", encoding="utf-8")
    return sdk


def run_gradle() -> Path:
    ensure_wrapper_jar()
    if host_tag() == "win":
        cmd = ["cmd", "/c", "gradlew.bat", "assembleDebug"]
    else:
        gw = ROOT / "gradlew"
        if not gw.exists():
            gw.write_text("#!/bin/sh\nDIR=$(CDPATH= cd -- \"$(dirname -- \"$0\")\" && pwd)\nexec java -jar \"$DIR/gradle/wrapper/gradle-wrapper.jar\" \"$@\"\n", encoding="utf-8")
            gw.chmod(gw.stat().st_mode | stat.S_IEXEC)
        cmd = [str(gw), "assembleDebug"]
    log("Gradle 编译中…")
    subprocess.check_call(cmd, cwd=ROOT)
    apk = ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
    if not apk.exists():
        raise FileNotFoundError("没找到 app-debug.apk")
    DIST.mkdir(exist_ok=True)
    out = DIST / "HourlyTalkClock.apk"
    shutil.copyfile(apk, out)
    log(f"完成: {out}")
    return out


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--github", default=DEFAULT_GITHUB)
    p.add_argument("--avatar")
    p.add_argument("--skip-sdk", action="store_true")
    args = p.parse_args()
    try:
        from PIL import Image  # noqa: F401
    except ImportError:
        log("pip install -r requirements-build.txt")
        return 1
    if shutil.which("java") is None:
        log("需要 JDK 17")
        return 1
    src = fetch_avatar(args.github, args.avatar)
    make_icons(src)
    if not args.skip_sdk:
        ensure_sdk(sdk_root())
    elif not (ROOT / "local.properties").exists():
        (ROOT / "local.properties").write_text(f"sdk.dir={sdk_root().as_posix()}\n", encoding="utf-8")
    run_gradle()
    return 0


if __name__ == "__main__":
    sys.exit(main())
