package calculadoracientifica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CalculadoraCientifica extends JFrame {

    boolean isDefaultMode = true;
    JPanel leftButtons = new JPanel(new GridLayout(5, 3, 5, 5));
    JPanel rightButtons = new JPanel(new GridLayout(4, 1, 5, 5));
    JPanel secundaryButtons = new JPanel(new GridLayout(4, 4, 5, 5));
    JTextField inputFields = new JTextField();
    JPanel renderedExpression = new JPanel();
    JLabel result = new JLabel();

    RenderExpression renderExpression = new RenderExpression();
    Solver solver = new Solver();

    public CalculadoraCientifica() {

        // 1. IO (LATEX, RESULT & INPUT)
        JPanel io = new JPanel(new BorderLayout());
        io.setPreferredSize(new Dimension(350, 175));
        io.setBackground(Color.decode("#f4f4f4"));

        // 1.1. Rendered Expression (Latex Version)
        renderedExpression.setPreferredSize(new Dimension(300, 75));
        renderedExpression.setBackground(Color.decode("#f4f4f4"));
        io.add(renderedExpression, BorderLayout.NORTH);

        // 1.2. Empty Fildes (C - Clear)
        JPanel emptyFields = new JPanel();
        emptyFields.setPreferredSize(new Dimension(50, 75));
        emptyFields.setBackground(Color.decode("#f4f4f4"));
        io.add(emptyFields, BorderLayout.WEST);
        addButton(emptyFields, "C");

        // 1.3. Input Fields (Sintax Version)
        inputFields.setPreferredSize(new Dimension(350, 50));
        inputFields.setBackground(Color.decode("#f4f4f4"));
        inputFields.setHorizontalAlignment(JTextField.RIGHT);
        inputFields.setFont(new Font("Arial", Font.PLAIN, 20));
        inputFields.setForeground(Color.BLACK);
        inputFields.setBorder(new EmptyBorder(0, 0, 0, 0));

        inputFields.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateFieldAndCalculate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateFieldAndCalculate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        io.add(inputFields, BorderLayout.CENTER);

        // 1.4. Result
        JPanel resultPanel = new JPanel();
        resultPanel.setPreferredSize(new Dimension(350, 50));
        resultPanel.setBackground(Color.decode("#f4f4f4"));
        result.setBackground(Color.decode("#f4f4f4"));

        result.setHorizontalAlignment(SwingConstants.RIGHT);
        result.setFont(new Font("Arial", Font.PLAIN, 18));
        result.setForeground(Color.decode("#545454"));

        resultPanel.add(result);
        io.add(resultPanel, BorderLayout.SOUTH);

        // 2. Buttons (KEYS 0-9, OPERATORS & Math Sign's)
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setPreferredSize(new Dimension(350, 275));
        buttons.setBackground(Color.WHITE);

        leftButtons.setPreferredSize(new Dimension(262, 275));
        leftButtons.setBackground(Color.WHITE);

        leftButtons.setBorder(new EmptyBorder(0, 0, 0, 5));

        rightButtons.setPreferredSize(new Dimension(87, 275));
        rightButtons.setBackground(Color.WHITE);

        secundaryButtons.setPreferredSize(new Dimension(350, 275));
        secundaryButtons.setBackground(Color.WHITE);
        secundaryButtons.setVisible(false);

        // 2.1. Buttons: Swap, Divide & Multiply
        addButton(leftButtons, "\u2194");
        addButton(leftButtons, "\u00F7");
        addButton(leftButtons, "\u00D7");

        // 2.2. Buttons: 0-9, comma & percentage
        for (int i = 7; i <= 9; i++) {
            addButton(leftButtons, String.valueOf(i));
        }

        for (int i = 4; i <= 6; i++) {
            addButton(leftButtons, String.valueOf(i));
        }

        for (int i = 1; i <= 3; i++) {
            addButton(leftButtons, String.valueOf(i));
        }

        addButton(leftButtons, "%");
        addButton(leftButtons, "0");
        addButton(leftButtons, ".");

        // 2.3. Buttons: Delete, Add,Subtract & Equal
        addButton(rightButtons, "\u2190");
        addButton(rightButtons, "+");
        addButton(rightButtons, "\u2212");
        addButton(rightButtons, "=");

        // 2.4. Buttons: Secundary Buttons
        addButton(secundaryButtons, "\u2194");
        addButton(secundaryButtons, "(");
        addButton(secundaryButtons, ")");
        addButton(secundaryButtons, "x!");
        addButton(secundaryButtons, "sen");
        addButton(secundaryButtons, "cos");
        addButton(secundaryButtons, "tan");
        addButton(secundaryButtons, "ln");
        addButton(secundaryButtons, "log");
        addButton(secundaryButtons, "xʸ");
        addButton(secundaryButtons, "π");
        addButton(secundaryButtons, "e");
        addButton(secundaryButtons, "√");
        addButton(secundaryButtons, "asen");
        addButton(secundaryButtons, "acos");
        addButton(secundaryButtons, "atan");

        // 2.4. Join Buttons
        buttons.add(leftButtons);
        buttons.add(rightButtons);
        buttons.add(secundaryButtons);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(io, BorderLayout.NORTH);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
    }

    private void addButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 20));
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);

        switch (text) {
            case "=" -> {
                button.setBackground(Color.decode("#27c76f"));
                button.setForeground(Color.WHITE);
                button.setSize(87, 100);
            }

            case "\u2190", "\u2212", "\u2194", "\u00D7", "\u00F7", "+" -> {
                button.setForeground(Color.decode("#27c76f"));
                button.setBackground(Color.WHITE);
            }
            case "C" -> {
                button.setForeground(Color.RED);
                button.setBackground(Color.decode("#f4f4f4"));
            }
            default -> {
                button.setBackground(Color.WHITE);
            }
        }

        button.addActionListener((ActionEvent e) -> {
            handleButtonClick(text);
        });

        panel.add(button);
    }

    private void handleButtonClick(String button) {

        String currentText = inputFields.getText();

        if ("Erro de Sintaxe!".equals(currentText)) {
            inputFields.setText("");
        }

        if (null != button) {
            switch (button) {
                case "\u2194" -> {
                    isDefaultMode = !isDefaultMode;
                    if (isDefaultMode) {
                        leftButtons.setVisible(true);
                        rightButtons.setVisible(true);
                        secundaryButtons.setVisible(false);
                    } else {
                        leftButtons.setVisible(false);
                        rightButtons.setVisible(false);
                        secundaryButtons.setVisible(true);
                    }
                }
                case "\u2190" -> {
                    if (currentText.length() > 0) {
                        String newText = currentText.substring(0, currentText.length() - 1);
                        inputFields.setText(newText);
                    }
                }
                case "C" -> {
                    inputFields.setText("");
                    result.setText("");
                    renderedExpression.removeAll();
                    renderedExpression.revalidate();
                    renderedExpression.repaint();
                }
                case "\u00F7" ->
                    setClickedButton("/");
                case "\u00D7" ->
                    setClickedButton("*");
                case "\u2212" ->
                    setClickedButton("-");
                case "x!" ->
                    setClickedButton("!");
                case "sen" ->
                    setClickedButton("sen()");
                case "asen" ->
                    setClickedButton("asen()");
                case "cos" ->
                    setClickedButton("cos()");
                case "acos" ->
                    setClickedButton("acos()");
                case "tan" ->
                    setClickedButton("tan()");
                case "log" ->
                    setClickedButton("log()");
                case "xʸ" ->
                    setClickedButton("()^()");
                case "√" ->
                    setClickedButton("(2)_()");
                case "π" ->
                    setClickedButton("pi");
                case "=" -> {
                    String resultExpression = solver.equalResult(currentText);
                    inputFields.setText(resultExpression);
                    result.setText("");
                }
                default -> {
                    setClickedButton(button);
                }
            }
        }
    }

    private void setClickedButton(String buttonText) {
        int caretPosition = inputFields.getCaretPosition();
        String currentText = inputFields.getText();

        String textBeforeCaret = currentText.substring(0, caretPosition);
        String textAfterCaret = currentText.substring(caretPosition);

        String newText = textBeforeCaret + buttonText + textAfterCaret;

        inputFields.setText(newText);
        inputFields.setCaretPosition(caretPosition + buttonText.length());
    }

    private void validateFieldAndCalculate() {

        String syntaxVersion = inputFields.getText();

        boolean isValidSyntax = solver.validateSyntax(syntaxVersion);

        if (isValidSyntax) {
            SwingWorker<BufferedImage, Void> worker;
            worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() {
                    return renderExpression.generateImage(syntaxVersion);
                }

                @Override
                protected void done() {
                    try {
                        BufferedImage image = get();
                        ImageIcon icon = new ImageIcon(image);
                        JLabel exp = new JLabel(icon);
                        renderedExpression.removeAll();
                        renderedExpression.add(exp);
                        renderedExpression.revalidate();
                        renderedExpression.repaint();
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(CalculadoraCientifica.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            worker.execute();
            String resultExpression = solver.Calculate(syntaxVersion);
            result.setText(resultExpression);
        } else {
            result.setText("");
            renderedExpression.removeAll();
            renderedExpression.revalidate();
            renderedExpression.repaint();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalculadoraCientifica().setVisible(true));
    }
}
