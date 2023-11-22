package calculadoracientifica;

import com.udojava.evalex.Expression;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.nfunk.jep.JEP;

class Solver {

    boolean validateSyntax(String syntaxVersion) {

        syntaxVersion = syntaxVersion.replaceAll("'", "*")
                .replaceAll("!", "")
                .replaceAll("log", "ln")
                .replaceAll("%", "");

        String[] spliteds = syntaxVersion.split("[^a-zA-Z]+");
        String regex = "\\s*(e|pi|ln|sen|cos|tan|asen|acos|atan)\\s*";

        Pattern pattern = Pattern.compile(regex);

        for (String splited : spliteds) {
            Matcher matcher = pattern.matcher(splited);
            if (!matcher.matches() && !splited.isEmpty()) {
                return false;
            }
        }

        JEP jep = new JEP();
        jep.setImplicitMul(true);

        try {
            jep.parseExpression(syntaxVersion);
            return !jep.hasError();
        } catch (Exception e) {
            return false;
        }
    }

    String Calculate(String syntaxVersion) {

        String equation = reBuilderSyntax(syntaxVersion);

        try {
            Expression expression = new Expression(equation);
            return expression.eval().toString();
        } catch (Exception e) {
            return "";
        }
    }

    ;
    
    String equalResult(String syntaxVersion) {

        String result = Calculate(syntaxVersion);

        if (result.equals("")) {
            return "Erro de Sintaxe!";
        }

        return result;
    }

    String reBuilderSyntax(String syntaxVersion) {

        syntaxVersion = syntaxVersion.replaceAll("\\bsen\\b", "sin")
                .replaceAll("\\basen\\b", "asin")
                .replaceAll("\\{", "(")
                .replaceAll("\\}", ")")
                .replaceAll("\\[", "(")
                .replaceAll("\\]", ")")
                .replaceAll("\\%", "*0.01")
                .replaceAll("(e|pi)\\^(\\d+\\.?\\d*)", "($1)^($2)")
                .replaceAll("\\be\\b", String.valueOf(Math.E))
                .replaceAll("\\bpi\\b", String.valueOf(Math.PI));

        if (syntaxVersion.contains("log")) {
            Pattern pattern = Pattern.compile("log\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(syntaxVersion);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                matcher.appendReplacement(result, "log10(" + x + ")");
            }
            matcher.appendTail(result);

            syntaxVersion = result.toString();
        }

        if (syntaxVersion.contains("_")) {
            Pattern pattern = Pattern.compile("\\((\\d+)\\)_\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(syntaxVersion);

            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int base = Integer.parseInt(matcher.group(2));
                int exponent = Integer.parseInt(matcher.group(1));

                String replacement = String.format("(%d)^(1/%d)", base, exponent);
                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);
            syntaxVersion = result.toString();
        }

        if (syntaxVersion.contains("!")) {
            Pattern pattern = Pattern.compile("(\\d+)!");
            Matcher matcher = pattern.matcher(syntaxVersion);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                int factorial = calcularFatorial(number);
                matcher.appendReplacement(result, String.valueOf(factorial));
            }
            matcher.appendTail(result);
            return result.toString();
        }

        return syntaxVersion;
    }

    static int calcularFatorial(int numero) {
        int fatorial = 1;

        for (int i = numero; i > 1; i--) {
            fatorial *= i;
        }

        return fatorial;
    }
}
