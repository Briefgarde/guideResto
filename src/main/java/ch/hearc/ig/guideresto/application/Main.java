package ch.hearc.ig.guideresto.application;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.FakeItems;
import ch.hearc.ig.guideresto.persistence.mapper.BasicEvalMapper;
import ch.hearc.ig.guideresto.persistence.mapper.CityMapper;
import ch.hearc.ig.guideresto.persistence.mapper.CompleteEvalMapper;
import ch.hearc.ig.guideresto.persistence.mapper.RestaurantMapper;
import ch.hearc.ig.guideresto.presentation.CLI;
import ch.hearc.ig.guideresto.presentation.CLIv2;

import java.util.*;

public class Main {

  public static void main(String[] args) {
    var scanner = new Scanner(System.in);
    var fakeItems = new FakeItems();
    var printStream = System.out;


    System.out.println("Choose your type : Base=1 or Updated=2 by my careful work (not)");
    //This is just a quick way for me to check how a functionalities work in the "correct" way, without rewriting the main
    String input = scanner.nextLine();
    if (input.equals("1")){
        CLI cli = new CLI(scanner, printStream, fakeItems);
        cli.start();
    } else {
        CLIv2 clIv2 = new CLIv2(scanner, printStream);
        clIv2.start();
    }
  }



}
