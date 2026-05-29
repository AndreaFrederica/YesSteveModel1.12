package com.elfmcys.yesstevemodel.client.entity;

import org.apache.commons.lang3.StringUtils;

public class AnimationStateMachine {

    private String currentAnimation = StringUtils.EMPTY;
    private String queuedAnimation = StringUtils.EMPTY;
    private String previousAnimation = StringUtils.EMPTY;

    public boolean hasAnimation() {
        return StringUtils.isNoneBlank(this.currentAnimation);
    }

    public String getCurrentAnimation() {
        return this.currentAnimation;
    }

    public void setCurrentAnimation(String animation) {
        this.currentAnimation = animation;
    }

    public String getQueuedAnimation() {
        return this.queuedAnimation;
    }

    public void setQueuedAnimation(String animation) {
        this.queuedAnimation = animation;
    }

    public String getPreviousAnimation() {
        return this.previousAnimation;
    }

    public void setPreviousAnimation(String animation) {
        this.previousAnimation = animation;
    }
}
