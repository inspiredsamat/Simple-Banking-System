import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Scanner;

public class Main {

    private static final String URL_PREFIX = "jdbc:sqlite:";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        String databaseUrl = URL_PREFIX + args[1];

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(databaseUrl);

        try (Connection con = dataSource.getConnection()) {
            createTable(con);

            while (true) {
                System.out.println("1. Create an account");
                System.out.println("2. Log into account");
                System.out.println("0. Exit");

                int choice = scanner.nextInt();
                scanner.close();
                System.out.println();

                switch (choice) {
                    case 1:
                        Account account = new Account();
                        System.out.println("Your card has been created");
                        System.out.println("Your card number:");
                        System.out.println(account.getCardNumber());
                        System.out.println("Your card PIN:");
                        System.out.println(account.getPinNumber() + "\n");
                        insertToAccount(con, account.getCardNumber(), account.getPinNumber(), account.getBalance());
                        break;
                    case 2:
                        searchAccount(con);
                        break;
                    case 0:
                        System.out.println("Bye!");
                        System.exit(0);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void searchAccount(Connection con) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter your card number:");
            String numberInput = scanner.next();
            String searchSQL = "SELECT number, pin, balance FROM card WHERE number = " + numberInput;
            System.out.println("Enter your PIN:");
            String pinInput = scanner.next();
            Statement statement = con.createStatement();
            ResultSet set = statement.executeQuery(searchSQL);

            while (set.next()) {
                if (set.getString("pin").equals(pinInput)) {
                    createAccountInterface(set.getInt("balance"), numberInput, con);
                    return;
                }
            }
        }
        System.out.println();
        System.out.println("Wrong card number or PIN!");
        System.out.println();
    }

    private static void createAccountInterface(int balance, String numberInput, Connection con) throws SQLException {
        try (Scanner scan = new Scanner(System.in)) {
            System.out.println();
            System.out.println("You have successfully logged in!");
            System.out.println();

            while (true) {
                System.out.println("1. Balance");
                System.out.println("2. Add income");
                System.out.println("3. Do transfer");
                System.out.println("4. Close account");
                System.out.println("5. Log out");
                System.out.println("0. Exit");

                int choiceInput = scan.nextInt();

                switch (choiceInput) {
                    case 1:
                        System.out.println("Balance: " + balance);
                        break;
                    case 2:
                        addIncome(numberInput, con);
                        System.out.println("Income was added!");
                        break;
                    case 3:
                        System.out.println();
                        System.out.println("Transfer");
                        doTransfer(con, numberInput);
                        break;
                    case 4:
                        closeAccount(numberInput, con);
                        System.out.println();
                        System.out.println("The account has been closed!");
                        return;
                    case 5:
                        System.out.println("You have successfully logged out!");
                        System.out.println();
                        return;
                    case 0:
                        System.out.println("Bye!");
                        System.exit(0);
                }
            }
        }
    }

    private static void doTransfer(Connection con, String currentCardNumber) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            String selectCardNumberToTransferSQL = "SELECT balance FROM card WHERE number = ?";
            String transferMoneySQL = "UPDATE card SET balance = balance + ? WHERE number = ?";
            String checkForMoneySQL = "SELECT balance from card WHERE number = ?";
            String decreaseMoneySQL = "UPDATE card set balance = balance - ? WHERE number = " + currentCardNumber;
            System.out.println("Enter card number:");
            String cardNumberInput = scanner.next();
            if (!isCorrectCardNumber(cardNumberInput)) {
                System.out.println("Probably you made a mistake in the card number. Please try again!");
                return;
            }

            PreparedStatement preparedStatement = con.prepareStatement(selectCardNumberToTransferSQL);
            preparedStatement.setString(1, cardNumberInput);
            ResultSet set = preparedStatement.executeQuery();
            int counter = 0;

            while (set.next()) {
                counter++;
            }

            if (counter == 0) {
                System.out.println("Such a card does not exist.");
                return;
            }

            System.out.println("Enter how much money you want to transfer:");
            int moneyToTransfer = scanner.nextInt();

            preparedStatement = con.prepareStatement(checkForMoneySQL);
            preparedStatement.setString(1, currentCardNumber);
            set = preparedStatement.executeQuery();

            if (set.getInt("balance") < moneyToTransfer) {
                System.out.println("Not enough money!");
                return;
            }

            preparedStatement = con.prepareStatement(transferMoneySQL);
            preparedStatement.setInt(1, moneyToTransfer);
            preparedStatement.setString(2, cardNumberInput);
            preparedStatement.executeUpdate();

            preparedStatement = con.prepareStatement(decreaseMoneySQL);
            preparedStatement.setInt(1, moneyToTransfer);
            preparedStatement.executeUpdate();
        }
    }

    private static boolean isCorrectCardNumber(String cardNumberInput) {
        int luhnAlgoCheck = 0;

        for (int i = 0; i < cardNumberInput.length() - 1; i++) {
            int currentDigit = cardNumberInput.charAt(i) - '0';
            if ((i + 1) % 2 == 1) {
                currentDigit *= 2;
            }

            if (currentDigit > 9) {
                currentDigit -= 9;
            }

            luhnAlgoCheck += currentDigit;
        }
        int lastDigit = cardNumberInput.charAt(cardNumberInput.length() - 1) - '0';

        if ((luhnAlgoCheck + lastDigit) % 10 == 0) {
            return true;
        }

        return false;
    }

    private static void closeAccount(String cardNumber, Connection con) throws SQLException {
        String deleteAccountSQL = "DELETE FROM card WHERE number = " + cardNumber;
        Statement statement = con.createStatement();
        statement.executeUpdate(deleteAccountSQL);
    }

    private static void addIncome(String cardNumber, Connection con) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter income:");
        int incomeInput = scanner.nextInt();
        String addIncomeSQL = "UPDATE card SET balance = balance + " + incomeInput + " WHERE number = " + cardNumber;
        String checkSQL = "SELECT balance from card WHERE number = " + cardNumber;

        Statement statement = con.createStatement();
        System.out.println("statement created");
        statement.executeUpdate(addIncomeSQL);
        System.out.println("statement executed");
        ResultSet set = statement.executeQuery(checkSQL);
        System.out.println("Current balance in card " + cardNumber + " is " + set.getInt("balance"));

        scanner.close();
    }

    private static void insertToAccount(Connection con, String cardNumber, String pinNumber, int balance)
            throws SQLException {
        String insertSQL = "INSERT INTO card (number, pin, balance) values (?, ?, ?)";

        PreparedStatement preparedStatement = con.prepareStatement(insertSQL);

        preparedStatement.setString(1, cardNumber);
        preparedStatement.setString(2, pinNumber);
        preparedStatement.setInt(3, balance);

        preparedStatement.executeUpdate();
    }

    private static void createTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS card " +
                "(id INTEGER PRIMARY KEY, " +
                "number TEXT NOT NULL, " +
                "pin TEXT NOT NULL, " +
                "balance INTEGER DEFAULT 0)";

        Statement statement = connection.createStatement();
        statement.executeUpdate(createTableSQL);
    }
}