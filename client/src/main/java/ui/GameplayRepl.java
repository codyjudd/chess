package ui;

import java.util.Scanner;

public class GameplayRepl {

    private final Scanner scanner = new Scanner(System.in);

    public void run() {

        System.out.println("Gameplay Started");

        while (true) {

            System.out.print("[game] >>> ");

            String line = scanner.nextLine();

            if (line.equals("help")) {
                help();
            }

            else if (line.equals("redraw")) {
                redraw();
            }

            else if (line.equals("leave")) {
                break;
            }
        }
    }

    private void help() {
        System.out.println("redraw");
        System.out.println("leave");
        System.out.println("move");
        System.out.println("resign");
    }

    private void redraw() {
        // TODO
    }
}
