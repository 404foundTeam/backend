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
            - The text-safe area MUST include a SOFT WHITE translucent panel (off-white/ivory, not pure #FFFFFF),
              with subtle background blur behind it (gentle Gaussian DOF). Rounded corners and very light shadow allowed.
              Target opacity 70–85%%; keep it LOW-DETAIL but NOT empty.
            - No objects/props/shadows may intrude into the text panel; keep a clean margin around it.
            - Keep rich detail and the focal subject only in the scene area; maintain color harmony and comfortable contrast.
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
                - Add a SOFT WHITE translucent panel (off-white, slight blur, rounded corners 28–36px).
                - Panel bounds depend on ratio:
                
                  • For 1:1 canvas: centered, covering ~75–80% of canvas height,
                    with outer margins of 3–4% on all sides.
                
                  • For 2:3 canvas: left/right margins ~5%, top 12–15%, bottom 85–88%.
                
                  • For 3:2 canvas: left/right margins ~6%, top 18–20%, bottom 82–84%.
                
                - The panel should appear dominant in size, leaving less background margin so that text area feels more emphasized.
                - Decorative thematic motifs may appear in periphery zones but NEVER overlap the text panel.
                - Motifs should be small-scale, low-contrast, complementary rather than competing.
                """;

            case T2_TEXT_BOTTOM -> """
                - TOP 55–60%: vivid PRODUCT SCENE (beverage/ingredients/details), high detail only in this zone.
                - Add a SOFT WHITE translucent text panel (rounded corners, subtle blur).
                - Panel bounds depend on ratio (strict):
            
                  • For 1:1 canvas:
                    left 2%, right 98%, top 78%, bottom 84%.
            
                  • For 2:3 canvas:
                    left 3%, right 97%, top 72%, bottom 78%.
            
                  • For 3:2 canvas:
                    left 2%, right 98%, top 77%, bottom 83%.
            
                - The panel must FLOAT inside the canvas at all times.
                - IMPORTANT: The panel must never touch or overlap any canvas edge.
                  Leave a clear margin (at least 2–3% on all sides).
                - Do not adapt panel size or position beyond these bounds.
                - No food/tableware/strong textures inside the panel.
                - Clip shadows/blur strictly within the panel bounds.
                """;
            case T3_TEXT_RIGHT -> """
                - LEFT 55–60%: vivid PRODUCT/INTERIOR SCENE with depth; high detail only on the left.
                - Add a vertical SOFT WHITE translucent panel (off-white, subtle blur) on the right side.
                - Panel bounds depend on ratio:
                
                  • For 1:1 canvas: left 62%, right 95%, top 8%, bottom 92%.
                  • For 2:3 canvas: left 64%, right 94%, top 6%, bottom 94%.
                  • For 3:2 canvas: left 60%, right 94%, top 10%, bottom 90%.
                
                - The panel must FLOAT inside the canvas; leave clear margins on all sides.
                - No objects or strong textures inside the panel.
                - Rounded corners and very light shadow allowed for separation.
                """;

        };
    }

    private static String safe(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n]+", " ").trim();
    }
}
