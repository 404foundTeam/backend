package com.found404.marketbee.sns.template.ai;

import com.found404.marketbee.sns.enums.CardRatio;
import com.found404.marketbee.sns.enums.SnsCardType;
import com.found404.marketbee.sns.enums.TemplateType;
import com.found404.marketbee.sns.enums.ThemeType;


public class TemplatePrompt {

    public static String buildForBackground(
            TemplateType template,
            CardRatio ratio,
            ThemeType theme,
            String generatedText,
            String storeName,
            SnsCardType cardType,
            String menuName
    ) {

        String copyPrompt = "";
        if (generatedText != null && !generatedText.isBlank()) {
            copyPrompt = """
        Marketing copy context (DO NOT render as text, use only as visual motif):
        %s
        """.formatted(safe(generatedText));
        }


        String themePrompt = switch (theme) {
            case WARM   -> "warm, cozy, soft light, friendly tones, gentle color palette";
            case MODERN -> "clean, minimal, subtle gradients, contemporary and elegant";
            case BRIGHT -> "vivid, lively, fresh, cheerful tones, high clarity";
        };

        String ratioPrompt = switch (ratio) {
            case SQUARE_1_1 -> "1:1 square";
            case RATIO_2_3  -> "2:3 vertical";
            case RATIO_3_2  -> "3:2 horizontal";
        };

        String menuPrompt = "";
        if (cardType == SnsCardType.NOTICE || cardType == SnsCardType.STORE_INTRO) {
            if (menuName != null && !menuName.isBlank()) {
                menuPrompt = "Representative menu to visualize (do NOT render text, use only as motif): "
                        + safe(menuName);
            }
        }

        String sizeStr = ratio.getWidth() + "x" + ratio.getHeight();

        String layout = textSafeAreaByTemplate(template);

        return """
            Create a high-quality promotional SCENE image for a social media card.

            Semantic alignment (VERY IMPORTANT):
            - From the copy keywords below, infer 3–5 core visual motifs, 1–2 materials/ingredients/objects, and a dominant color palette.
            - Reflect them clearly in the scene composition WITHOUT rendering any typography.
            Copy keywords (NOT for text, motif only):
            %s

            Business context (if applicable):
            - Use these hints to choose plausible objects/setting, never render the name itself:
            %s

            Style/Theme: %s
            Aspect: %s
            Target size (pixels): %s

            Strict Aspect Ratio & Framing:
            - Final render MUST be exactly %s (no letterboxing, no side bars, no top/bottom padding).
            - If content conflicts with aspect, adjust camera/framing/cropping while preserving composition rules.

            Composition & Text-safe Area (mandatory):
            %s

                Hard constraints:
                - DO NOT render any letters in any language (including Korean/Hangul), numbers, signage, captions, logos, or watermark.
                - Do NOT render or hint at the store name "%s".
                - People-free image: no humans, faces, hands, body parts, silhouettes or crowds; also exclude people in posters,
                  reflections, paintings, statues, mannequins or screens.
                - The text-safe area must remain visually clear and uncluttered.
                  • Background color/texture is allowed.
                  • Do NOT place strong objects, food, props, or focal motifs inside this area.
                  • Ensure nearby objects do not intrude into the clear zone; keep a clean margin.
                - Keep rich detail and the focal subject only in the designated scene area; maintain color harmony and comfortable contrast.
            """
                .formatted(
                        copyPrompt,
                        menuPrompt,
                        themePrompt,
                        ratioPrompt,
                        sizeStr,
                        sizeStr,
                        layout,
                        safe(storeName)
                );
    }


    private static String textSafeAreaByTemplate(TemplateType t) {
        return switch (t) {
            case T1_TEXT_ONLY -> """
            - TEXT-FOCUSED layout, but DO NOT render typography.
            - Reserve a large central zone as relatively clear space (no overlapping food/ingredients/objects).
            - Ratio-specific guidance:
            
              • For 1:1 canvas: central ~75–80% of canvas height must stay relatively uncluttered,
                with 3–4% margins on all sides.
              • For 2:3 canvas: keep clear area between top 12–15% and bottom 85–88%.
              • For 3:2 canvas: keep clear area between top 18–20% and bottom 82–84%.
            
            - Decorative motifs may appear only at the periphery and must not intrude into the clear zone.
            """;

            case T2_TEXT_BOTTOM -> """
            - TOP 55–60%: vivid PRODUCT SCENE (beverage/ingredients/details), rich detail only here.
            - Reserve a horizontal band near the bottom as clear space (no overlapping objects/props).
            - Ratio-specific guidance:
            
              • For 1:1 canvas: keep area between 78%–84% uncluttered.
              • For 2:3 canvas: keep area between 72%–78% uncluttered.
              • For 3:2 canvas: keep area between 77%–83% uncluttered.
            
            - Clear space must never touch edges directly; leave 2–3% margins.
            """;

            case T3_TEXT_RIGHT -> """
            - LEFT 55–60%: vivid PRODUCT/INTERIOR SCENE with depth, high detail only on the left.
            - Reserve a vertical band on the right side as clear space (no overlapping food/props).
            - Ratio-specific guidance:
            
              • For 1:1 canvas: keep area between 62%–95% horizontally uncluttered.
              • For 2:3 canvas: keep area between 64%–94% horizontally uncluttered.
              • For 3:2 canvas: keep area between 60%–94% horizontally uncluttered.
            
            - Clear space must float inside the canvas; leave margins on all sides.
            """;
        };
    }


    private static String safe(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n]+", " ").trim();
    }
}
