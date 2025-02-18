package com.openjfx.models;

import java.util.Objects;

/**
 * Represents a Choice with various attributes such as class reference, first name, last name, and
 * multiple choices.
 */
public class Choice {

  private String classRef;
  private String firstName;
  private String lastName;
  private String choice1;
  private String choice2;
  private String choice3;
  private String choice4;
  private String choice5;
  private String choice6;

  /**
   * Constructs a new Choice with the specified attributes.
   *
   * @param classRef  the class reference
   * @param firstName the first name of the student
   * @param lastName  the last name of the student
   * @param choice1   the first choice (Most preferred) of the student
   * @param choice2   the second choice of the student
   * @param choice3   the third choice of the student
   * @param choice4   the fourth choice of the student
   * @param choice5   the fifth choice of the student
   * @param choice6   the sixth choice (Least preferred) of the student
   */

  public Choice(String classRef, String firstName, String lastName, String choice1, String choice2,
      String choice3, String choice4, String choice5, String choice6) {
    this.classRef = classRef;
    this.firstName = firstName;
    this.lastName = lastName;
    this.choice1 = choice1;
    this.choice2 = choice2;
    this.choice3 = choice3;
    this.choice4 = choice4;
    this.choice5 = choice5;
    this.choice6 = choice6;
  }

  /**
   * Gets the class reference.
   *
   * @return the class reference
   */
  public String getClassRef() {
    return classRef;
  }

  /**
   * Sets the class reference.
   *
   * @param classRef the class reference
   */
  public void setClassRef(String classRef) {
    this.classRef = classRef;
  }

  /**
   * Gets the first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the first choice.
   *
   * @return the first choice
   */
  public String getChoice1() {
    return choice1;
  }

  /**
   * Sets the first choice.
   *
   * @param choice1 the first choice
   */
  public void setChoice1(String choice1) {
    this.choice1 = choice1;
  }

  /**
   * Gets the second choice.
   *
   * @return the second choice
   */
  public String getChoice2() {
    return choice2;
  }

  /**
   * Sets the second choice.
   *
   * @param choice2 the second choice
   */
  public void setChoice2(String choice2) {
    this.choice2 = choice2;
  }

  /**
   * Gets the third choice.
   *
   * @return the third choice
   */
  public String getChoice3() {
    return choice3;
  }

  /**
   * Sets the third choice.
   *
   * @param choice3 the third choice
   */
  public void setChoice3(String choice3) {
    this.choice3 = choice3;
  }

  /**
   * Gets the fourth choice.
   *
   * @return the fourth choice
   */
  public String getChoice4() {
    return choice4;
  }

  /**
   * Sets the fourth choice.
   *
   * @param choice4 the fourth choice
   */
  public void setChoice4(String choice4) {
    this.choice4 = choice4;
  }

  public String getChoice5() {
    return choice5;
  }

  /**
   * Sets the fifth choice.
   *
   * @param choice5 the fifth choice
   */
  public void setChoice5(String choice5) {
    this.choice5 = choice5;
  }

  /**
   * Gets the sixth choice.
   *
   * @return the sixth choice
   */
  public String getChoice6() {
    return choice6;
  }

  /**
   * Sets the sixth choice.
   *
   * @param choice6 the sixth choice
   */
  public void setChoice6(String choice6) {
    this.choice6 = choice6;
  }

  /**
   * Checks if this Choice is equal to another object.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Choice choice = (Choice) o;
    return Objects.equals(classRef, choice.classRef) &&
        Objects.equals(firstName, choice.firstName) &&
        Objects.equals(lastName, choice.lastName) &&
        Objects.equals(choice1, choice.choice1) &&
        Objects.equals(choice2, choice.choice2) &&
        Objects.equals(choice3, choice.choice3) &&
        Objects.equals(choice4, choice.choice4) &&
        Objects.equals(choice5, choice.choice5) &&
        Objects.equals(choice6, choice.choice6);
  }

  /**
   * Generates a hash code for this Choice.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        classRef,
        firstName,
        lastName,
        choice1,
        choice2,
        choice3,
        choice4,
        choice5,
        choice6
    );
  }

  /**
   * Returns a string representation of this Choice.
   *
   * @return the string representation
   */
  @Override
  public String toString() {
    return "Choice{" +
        "classRef='" + classRef + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", choice1='" + choice1 + '\'' +
        ", choice2='" + choice2 + '\'' +
        ", choice3='" + choice3 + '\'' +
        ", choice4='" + choice4 + '\'' +
        ", choice5='" + choice5 + '\'' +
        ", choice6='" + choice6 + '\'' +
        '}';
  }
}
