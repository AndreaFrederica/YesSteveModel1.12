/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.github.bsideup.jabel.Desugar;

/**
 * 仅存储名称和播放循环类型的类
 */
@Desugar
public record RawAnimation(String animationName, ILoopType loopType) {
}
