# Yes Steve Model

[旧版 Yes Steve Model 模组](https://github.com/YesSteveModel/LgeacyYSM)的 Minecraft 1.12.2 完整移植。

## 概述

Yes Steve Model 是一个修改原版玩家模型的 Minecraft 模组。

它采用了 GeckoLib 作为核心，使用 Minecraft 基岩版模型和动画文件。从而使玩家能够随心所欲的自定义玩家模型和动画。

## 状况

### 兼容性

模组需要 [MixinBooter](https://github.com/CleanroomMC/MixinBooter) 作为前置。

- 模型
  - 兼容 2.0.0 以下版本三种格式的模型
  - 暂不兼容 2.0.0 以上版本文件夹和 ZIP 格式的模型
  - 永远不会兼容 2.0.0 以上版本 YSM 加密格式的模型

- 模组
  - 兼容 [Aqua Acrobatics](https://github.com/embeddedt/aquaacrobatics) 的游泳动作
  - 兼容 [Oceanic Expanse](https://github.com/SirSquidly/Oceanic-Expanse)、[Trident Mod](https://github.com/jiGGO1/Trident)、[Future MC](https://github.com/thedarkcolour/Future-MC) 的三叉戟动作
  - 兼容 [Crossbows Backport](https://github.com/SmileycorpMC/crossbows-backport)、[Crossbow Mod](https://github.com/jbredwards/Crossbow-Mod) 的弩动作
  - 兼容 [Deeper Depths](https://github.com/SmileycorpMC/Deeper-Depths) 的望远镜动作
  - 兼容 [Mekanism Mixin Help](https://github.com/sddsd2332/MekanismMixinHelp) 的 HDPE 鞘翅

### 已知问题

- 模型状态似乎有污染问题，导致有时模型块变换有误
- 视角旋转过快时，头部角度会跳变
- 模型预览界花和草光照不对
- 箭矢动画似乎不应出现在模型预览界面
- 部分文字颜色不对，这是因为新版文本组件会自动重置颜色，旧版 I18n 处理 String 时不会
- RenderFirstPlayerBackground 未经测试，暂未启用，需要找模型案例
- 自发光纹理未经测试，需要找模型案例
- 刚进游戏时会有一瞬间报纹理丢失错误，这是因为纹理还未来得及同步

## 致谢

- [YSM 开发组](https://github.com/YesSteveModel)开源了旧版 YSM
- [一只大胡哩](https://github.com/Huli-fox)和[凯西](https://github.com/kaixiten)制作了最初的 [1.7.10 版本](https://github.com/Huli-fox/YesSteveModel-Unofficial)
- [sddsd2332](https://github.com/sddsd2332) 制作了 [1.12.2 移植版](https://github.com/sddsd2332/YesSteveModel-Unofficial)，并为本项目做出了杰出贡献
- [GeckoLib](https://github.com/bernie-g/geckolib) 的所有开发者
- [kappa-maintainer](https://github.com/kappa-maintainer) 制作的 [SauriaLib](https://github.com/kappa-maintainer/geckolib)，GeckoLib 的延续
