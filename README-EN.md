# HiResShot

![Logo](docs/icon.png)

**HiResShot** is a Minecraft screenshot enhancement mod that allows players to take screenshots with resolutions far exceeding the current screen resolution (e.g., 4K, 8K, or even higher).

It works by resizing the internal Framebuffer to achieve native high-resolution rendering, rather than simple pixel stretching. This mod is designed to solve the problem of insufficient screenshot resolution and is perfect for creating wallpapers, posters, or high-detail prints.

Supported Versions: **1.12.2 (Forge)** / **1.20.1 (Forge/NeoForge)** / **1.21.1 (NeoForge)**

---

## Features

*   **Ultra-High Resolution**: Supports screenshots up to 64x the screen resolution (limited by GPU VRAM).
*   **Shader Compatibility**: Specialized "Real-time Mode" optimized for shader packs, solving issues with black screens or underexposure.
*   **Bypass Hardware Limits**: Provides "CPU Upscale Mode", allowing the creation of massive resolution images (e.g., 16K+) via algorithmic synthesis even if GPU VRAM is insufficient.
*   **Custom Resolution**: Supports setting specific width and height (e.g., 21:9 ultrawide or portrait mobile wallpapers).
*   **Auto Cleanup**: Automatically hides the GUI (interface), crosshair, and player entity (to prevent self-shadow occlusion) during capture.

## Capture Modes

This mod provides three capture modes to adapt to different needs and hardware conditions:

### 1. Real-time Mode (Recommended)
*   **Best for**: Shaders, TAA anti-aliasing, or motion blur.
*   **Mechanism**: Resizes the game resolution to the target size and continues running for a specified number of frames (configurable), waiting for the shader rendering pipeline to stabilize before saving.
*   **Pros**: Best compatibility, WYSIWYG (What You See Is What You Get). The game will lag briefly during capture.

### 2. Instant Mode
*   **Best for**: Vanilla graphics, no complex shaders.
*   **Mechanism**: Instantly freezes the frame, renders multiple frames in the background, and saves.
*   **Pros**: Fast, minimal interference.

### 3. CPU Upscale Mode
*   **Best for**: Extremely high target resolutions (e.g., 32K) that exceed the graphics card's capability.
*   **Mechanism**: First renders a base image at the maximum size allowed by the GPU (e.g., 16K), then uses the CPU for high-quality Bicubic Interpolation to upscale to the target size.
*   **Pros**: Can generate images of arbitrary size, preventing game crashes due to VRAM overflow.

## Usage

1.  Press **F9** (default) in-game to trigger a screenshot.
2.  Screenshot files are saved in the `.minecraft/screenshots/` directory with an `_hrs` suffix.
3.  **Configuration**:
    *   **1.12.2**: Click Mods -> HiResShot -> Config.
    *   **1.20.1+**: Requires **Cloth Config API**. Open the graphical interface by clicking the config button in the Mods list.

## Configuration Details

*   **Multiplier**: Magnification factor (2x - 64x).
*   **Use Custom Resolution**: Whether to enable custom resolution (overrides Multiplier).
*   **Capture Mode**: Select one of the three modes mentioned above.
*   **Real-time Delay**: Wait frames for Real-time Mode. If the screenshot is dark or shaders haven't fully loaded, increase this value (Recommended: 20-60).
*   **Hide Player**: Whether to hide the player model (prevents unwanted shadows under shaders).
*   **Hide GUI**: Whether to hide the GUI (including the hand in first-person view).

## Requirements

*   **1.20.1 / 1.21.1**: Requires [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to display the configuration interface.