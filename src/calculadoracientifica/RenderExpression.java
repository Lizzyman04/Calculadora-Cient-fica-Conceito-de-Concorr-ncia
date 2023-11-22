package calculadoracientifica;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import org.scilab.forge.jlatexmath.*;

public class RenderExpression {

    public BufferedImage generateImage(String syntaxVersion) {

        String latexExpression = reBuilderLatex(syntaxVersion);

        int width = 300;
        int height = 75;

        TeXFormula formula = new TeXFormula(latexExpression);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setBackground(Color.decode("#f4f4f4"));
        g2.clearRect(0, 0, width, height);
        
        JLabel jl = new JLabel();

        jl.setOpaque(true);
        jl.setBackground(Color.decode("#f4f4f4"));

        jl.setForeground(new Color(0, 0, 0));
        icon.paintIcon(jl, g2, 0, 0);

        return image;
    }

    String reBuilderLatex(String syntaxVersion) {
        syntaxVersion = syntaxVersion.replaceAll("\\bsen\\b", "\\\\sin")
                .replaceAll("\\basen\\b", "\\\\arcsin")
                .replaceAll("\\bcos\\b", "\\\\cos")
                .replaceAll("\\bacos\\b", "\\\\arccos")
                .replaceAll("\\*", "•")
                .replaceAll("\\%", "\\\\%")
                .replaceAll("(e|pi)\\^(\\d+\\.?\\d*)", "($1)^{$2}")
                .replaceAll("pi", "π");

        if (syntaxVersion.contains("log")) {
            Pattern pattern = Pattern.compile("log\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(syntaxVersion);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                matcher.appendReplacement(result, "\\\\log_{10}(" + x + ")");
            }
            matcher.appendTail(result);

            syntaxVersion = result.toString();
        }
        
                if (syntaxVersion.contains("^")) {
            Pattern pattern = Pattern.compile("\\((\\d+)\\)\\^\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(syntaxVersion);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int base = Integer.parseInt(matcher.group(1));
                int exponent = Integer.parseInt(matcher.group(2));

                String replacement = String.format("%d^{%d}", base, exponent);
                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);
            syntaxVersion = result.toString();
        }

        if (syntaxVersion.contains("_")) {
            Pattern pattern = Pattern.compile("\\((\\d+)\\)_\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(syntaxVersion);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                int radicand = Integer.parseInt(matcher.group(2));

                String replacement = String.format("\\\\sqrt[%d]{%d}", index, radicand);
                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);
            syntaxVersion = result.toString();
        }

        return syntaxVersion;
    }

}
