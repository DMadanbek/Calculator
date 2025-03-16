import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        ArrayList<String> history = new ArrayList<>();
        //Основной интерфейс калькулятора
        while (true) {
            System.out.println(" 1. Вычислить операцию" +
                    "\n 2. Посмотреть историю" +
                    "\n 3. Очистить историю" +
                    "\n 4. Воспроизвести запись из истории");

            int chosen = input.nextInt();
            input.nextLine();

            switch (chosen) {
                case 1:
                    System.out.println("Введите выражение:");
                    String expression = input.nextLine().replaceAll("\\s+", "");
                    history.add(expression);
                    try {
                        List<String> tokens = tokenize(expression);
                        List<String> rpn = toRPN(tokens);
                        double result = evaluateRPN(rpn);
                        System.out.println("Результат: " + result);
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                    break;
                case 2:
                    for (int i = 0; i < history.size(); i++) {
                        System.out.println(i + 1 + ". " + history.get(i));
                    }
                    break;
                case 3:
                    history.clear();
                    break;
                case 4:
                    System.out.println("Введите номер записи:");
                    int historyNum = input.nextInt();

                    try {
                        List<String> tokens = tokenize(history.get(historyNum - 1));
                        List<String> rpn = toRPN(tokens);
                        double result = evaluateRPN(rpn);
                        System.out.println("Результат: " + result);
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                    break;
            }

            System.out.println("Хотите продолжить? (y/n)");
            String cont = input.nextLine().trim().toLowerCase();
            if (cont.equals("n")) {
                System.out.println("Спасибо за использование калькулятора!");
                System.exit(0);
            }
        }
    }

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder num = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || "+-*/%^(".contains(expression.charAt(i - 1) + "")))) {
                num.append(c);
            } else {
                if (num.length() > 0) {
                    tokens.add(num.toString());
                    num.setLength(0);
                }
                if ("()+-*/%^,".indexOf(c) >= 0) {
                    tokens.add(String.valueOf(c));
                } else if (Character.isLetter(c)) {
                    StringBuilder func = new StringBuilder();
                    while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                        func.append(expression.charAt(i++));
                    }
                    i--;
                    tokens.add(func.toString());
                }
            }
        }
        if (num.length() > 0) {
            tokens.add(num.toString());
        }
        return tokens;
    }

    private static List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();
        Map<String, Integer> precedence = Map.of(
                "+", 1, "-", 1, "*", 2, "/", 2, "%", 2, "^", 3
        );

        for (String token : tokens) {
            if (token.matches("-?\\d+(\\.\\d+)?")) { // Числа
                output.add(token);
            } else if ("+-*/%^".contains(token)) { // Операторы
                while (!operators.isEmpty() && precedence.containsKey(operators.peek()) &&
                        precedence.get(operators.peek()) >= precedence.get(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) { // Скобки
                operators.push(token);
            } else if (token.equals(")")) { // Закрывающая скобка
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                operators.pop(); // Убираем открывающую скобку
            } else if (List.of("abs", "sqrt", "round", "pow").contains(token)) { // Функции
                operators.push(token);
            } else if (token.equals(",")) { // Аргументы функций
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
            }
        }

        while (!operators.isEmpty()) { // Добавляем оставшиеся операторы
            output.add(operators.pop());
        }

        return output;
    }

    private static double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(token));
            } else {
                switch (token) {
                    case "+" -> stack.push(stack.pop() + stack.pop());
                    case "-" -> {
                        double b = stack.pop(), a = stack.pop();
                        stack.push(a - b);
                    }
                    case "*" -> stack.push(stack.pop() * stack.pop());
                    case "/" -> {
                        double b = stack.pop(), a = stack.pop();
                        if (b == 0) throw new ArithmeticException("Деление на ноль");
                        stack.push(a / b);
                    }
                    case "%" -> {
                        double b = stack.pop(), a = stack.pop();
                        stack.push(a % b);
                    }
                    case "^" -> {
                        double b = stack.pop(), a = stack.pop();
                        stack.push(Math.pow(a, b));
                    }
                    case "abs" -> stack.push(Math.abs(stack.pop()));
                    case "sqrt" -> {
                        double a = stack.pop();
                        if (a < 0) throw new ArithmeticException("Корень из отрицательного числа");
                        stack.push(Math.sqrt(a));
                    }
                    case "round" -> stack.push((double) Math.round(stack.pop()));
                    case "pow" -> {
                        double exponent = stack.pop();
                        double base = stack.pop();
                        stack.push(Math.pow(base, exponent));
                    }
                }
            }
        }

        return stack.pop();
    }
}
