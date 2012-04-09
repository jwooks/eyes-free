/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.marvin.talkback.speechrules;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.marvin.talkback.R;
import com.google.android.marvin.talkback.Utterance;

import java.util.LinkedList;

/**
 * @author alanv@google.com (Alan Viverette)
 */
public class NodeProcessor {
    private static final String DESCRIPTION_SEPARATOR = ". ";

    private final LinkedList<NodeSpeechRule> mRules = new LinkedList<NodeSpeechRule>();
    private final Context mContext;

    public NodeProcessor(Context context) {
        mContext = context;

        loadRules();
    }

    /**
     * Returns the best description for a node.
     * 
     * @param node The node to describe.
     * @param event The source event, may be {@code null} when called with
     *            non-source nodes.
     * @return The best description for a node.
     */
    public CharSequence process(AccessibilityNodeInfo node, AccessibilityEvent event) {
        final StringBuilder populator = new StringBuilder();

        // Append the control's checked state.
        if (node.isCheckable()) {
            if (node.isChecked()) {
                appendTextToBuilder(mContext.getString(R.string.value_checked), populator);
            } else {
                appendTextToBuilder(mContext.getString(R.string.value_not_checked), populator);
            }
        }

        // Add node text
        final CharSequence text = processWithRules(node, event);

        if (!TextUtils.isEmpty(text)) {
            appendTextToBuilder(text, populator);
        }

        if (TextUtils.isEmpty(populator)) {
            return null;
        }

        // Append the control's disabled state.
        if (!node.isEnabled()) {
            appendTextToBuilder(mContext.getString(R.string.value_disabled), populator);
        }

        return populator;
    }

    /**
     * Loads the default rule set.
     */
    private void loadRules() {
        mRules.add(new RuleImageView());
        mRules.add(new RuleEditText());
        mRules.add(new RuleSeekBar());
        mRules.add(new RuleSimpleTemplate(android.widget.Spinner.class, R.string.template_spinner));
        mRules.add(new RuleSimpleTemplate(android.webkit.WebView.class, R.string.value_web_view));

        // Always add the default rule last.
        mRules.add(new RuleDefault());
    }

    /**
     * Processes the specified node using a series of speech rules.
     * 
     * @param node The node to process.
     * @param event The source event, may be {@code null} when called with
     *            non-source nodes.
     * @return A string representing the given node, or {@code null} if the node
     *         could not be processed.
     */
    private CharSequence processWithRules(AccessibilityNodeInfo node, AccessibilityEvent event) {
        for (NodeSpeechRule rule : mRules) {
            if (rule.accept(node)) {
                return rule.format(mContext, node, event);
            }
        }

        return null;
    }

    /**
     * Returns the verbose description for a node.
     * 
     * @param node The node to describe.
     * @return The verbose description for a node.
     */
    public static CharSequence processVerbose(Context context, AccessibilityNodeInfo node) {
        // TODO(alanv): This method shouldn't be static. Or here at all?

        final StringBuilder populator = new StringBuilder();

        // Append control's interaction capabilities or disabled state
        if (node.isEnabled()) {
            if (node.isClickable()) {
                appendTextToBuilder(context.getString(R.string.value_clickable), populator);
            }

            if (node.isLongClickable()) {
                appendTextToBuilder(context.getString(R.string.value_long_clickable), populator);
            }
        }

        return populator;
    }

    /**
     * Helper for appending the given {@link String} to the existing text in an
     * {@link Utterance}. Also adds a punctuation separator.
     * 
     * @param text {@link String} of text to append.
     * @param builder {@link StringBuilder} to which text is appended.
     */
    private static void appendTextToBuilder(CharSequence text, StringBuilder builder) {
        builder.append(text);
        builder.append(DESCRIPTION_SEPARATOR);
    }
}