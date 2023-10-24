package ch.hearc.ig.guideresto.application;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.FakeItems;
import ch.hearc.ig.guideresto.persistence.mapper.CityMapper;
import ch.hearc.ig.guideresto.persistence.mapper.IMapper; 
import ch.hearc.ig.guideresto.presentation.CLI;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class Main {

  public static void main(String[] args) {
    CityMapper test = new CityMapper();
    City test1 = test.findByID(1);
    System.out.println("TEst " + test1.getCityName() + test1.getZipCode());
    Set<City> test2 = test.findAll();
    for (City city: test2) {
      System.out.println("Via find all" + city.getZipCode() + " " + city.getCityName() + "this is still for a test commit");
    }
    //this is for a test commit
  //  var scanner = new Scanner(System.in);
  //  var fakeItems = new FakeItems();
  //  var printStream = System.out;
  //  IMapper cityMapper = new CityMapper();
  //  var cli = new CLI(scanner, printStream, fakeItems, cityMapper);

  //  cli.start();
  }
}
