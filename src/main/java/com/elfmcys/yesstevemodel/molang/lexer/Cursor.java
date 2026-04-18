/*
 * This file is part of molang, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elfmcys.yesstevemodel.molang.lexer;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Mutable class that tracks the position of characters
 * when performing lexical analysis
 *
 * <p>Can be used to show the position of lexical errors
 * in a human-readable way</p>
 *
 * @since 3.0.0
 */
public final class Cursor implements Cloneable {

    private int index = 0;
    private int line = 0;
    private int column = 0;

    public Cursor(final int line, final int column) {
        this.line = line;
        this.column = column;
    }

    public Cursor() {
    }

    public int index() {
        return this.index;
    }

    public int line() {
        return this.line;
    }

    public int column() {
        return this.column;
    }

    public void push(final int character) {
        this.index++;
        if (character == '\n') {
            // if it's a line break,
            // reset the column
            this.line++;
            this.column = 1;
        } else {
            this.column++;
        }
    }

    @Override
    public @Nonnull Cursor clone() {
        return new Cursor(this.line, this.column);
    }

    @Override
    public String toString() {
        return "line " + this.line + ", column " + this.column;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Cursor that = (Cursor) o;
        return this.line == that.line && this.column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.line, this.column);
    }

}
