package fr.layer4.hhsl.prompt;

public interface Prompter {

    String doublePromptForPassword();

    String promptForRootPassword();

    String promptForPassword(String message);

    String prompt(String message);
}
