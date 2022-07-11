import java.util.Random;

public class Account {

    private static final String INITIAL_NUMBERS = "400000";

    private String cardNumber;
    private String pinNumber;
    private int balance;

    private Random randomGenerator;

    public Account() {
        this.cardNumber = createCardNumber();
        this.pinNumber = createPinNumber();
        this.balance = 0;
    }

    private String createCardNumber() {
        String newCardNumber = "";
        newCardNumber += INITIAL_NUMBERS;

        int luhnAlgo = 0;
        randomGenerator = new Random();

        for (int i = 0; i < 9; i++) {
            newCardNumber = newCardNumber + "" + randomGenerator.nextInt(10);
        }

        for (int i = 0; i < newCardNumber.length(); i++) {
            int currentDigit = newCardNumber.charAt(i) - '0';
            if ((i + 1) % 2 == 1) {
                currentDigit *= 2;
            }

            if (currentDigit > 9) {
                currentDigit -= 9;
            }

            luhnAlgo += currentDigit;
        }

        newCardNumber = newCardNumber + "" + ((10 - (luhnAlgo % 10)) % 10);
        return newCardNumber;
    }

    private String createPinNumber() {
        String newPinNumber = "";
        for (int i = 0; i < 4; i++) {
            newPinNumber += randomGenerator.nextInt(10);
        }
        return newPinNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
