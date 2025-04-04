package com.openjfx.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing fulfillment scores for student workshop assignments.
 *
 * @author mian
 */
public class FulfillmentScore {

  private Integer id;
  private String studentId;
  private String classRef;
  private String firstName;
  private String lastName;
  private Integer choice1Score;
  private Integer choice2Score;
  private Integer choice3Score;
  private Integer choice4Score;
  private Integer choice5Score;
  private Integer choice6Score;
  private Integer studentTotalScore;
  private LocalDateTime calculationTimestamp;
  private Double overallFulfillmentPercentage;
  private Integer totalStudents;
  private Integer totalScore;
  private Double maxPossibleScore;

  /**
   * Default constructor.
   *
   * @author mian
   */
  public FulfillmentScore() {
  }

  /**
   * Gets the ID of the fulfillment score.
   *
   * @return the ID
   * @author mian
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the ID of the fulfillment score.
   *
   * @param id the ID to set
   * @author mian
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the student ID.
   *
   * @return the student ID
   * @author mian
   */
  public String getStudentId() {
    return studentId;
  }

  /**
   * Sets the student ID.
   *
   * @param studentId the student ID to set
   * @author mian
   */
  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  /**
   * Gets the class reference.
   *
   * @return the class reference
   * @author mian
   */
  public String getClassRef() {
    return classRef;
  }

  /**
   * Sets the class reference.
   *
   * @param classRef the class reference to set
   * @author mian
   */
  public void setClassRef(String classRef) {
    this.classRef = classRef;
  }

  /**
   * Gets the first name.
   *
   * @return the first name
   * @author mian
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the first name to set
   * @author mian
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the last name.
   *
   * @return the last name
   * @author mian
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the last name to set
   * @author mian
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the score for choice 1.
   *
   * @return the choice 1 score
   * @author mian
   */
  public Integer getChoice1Score() {
    return choice1Score;
  }

  /**
   * Sets the score for choice 1.
   *
   * @param choice1Score the choice 1 score to set
   * @author mian
   */
  public void setChoice1Score(Integer choice1Score) {
    this.choice1Score = choice1Score;
  }

  /**
   * Gets the score for choice 2.
   *
   * @return the choice 2 score
   * @author mian
   */
  public Integer getChoice2Score() {
    return choice2Score;
  }

  /**
   * Sets the score for choice 2.
   *
   * @param choice2Score the choice 2 score to set
   * @author mian
   */
  public void setChoice2Score(Integer choice2Score) {
    this.choice2Score = choice2Score;
  }

  /**
   * Gets the score for choice 3.
   *
   * @return the choice 3 score
   * @author mian
   */
  public Integer getChoice3Score() {
    return choice3Score;
  }

  /**
   * Sets the score for choice 3.
   *
   * @param choice3Score the choice 3 score to set
   * @author mian
   */
  public void setChoice3Score(Integer choice3Score) {
    this.choice3Score = choice3Score;
  }

  /**
   * Gets the score for choice 4.
   *
   * @return the choice 4 score
   * @author mian
   */
  public Integer getChoice4Score() {
    return choice4Score;
  }

  /**
   * Sets the score for choice 4.
   *
   * @param choice4Score the choice 4 score to set
   * @author mian
   */
  public void setChoice4Score(Integer choice4Score) {
    this.choice4Score = choice4Score;
  }

  /**
   * Gets the score for choice 5.
   *
   * @return the choice 5 score
   * @author mian
   */
  public Integer getChoice5Score() {
    return choice5Score;
  }

  /**
   * Sets the score for choice 5.
   *
   * @param choice5Score the choice 5 score to set
   * @author mian
   */
  public void setChoice5Score(Integer choice5Score) {
    this.choice5Score = choice5Score;
  }

  /**
   * Gets the score for choice 6.
   *
   * @return the choice 6 score
   * @author mian
   */
  public Integer getChoice6Score() {
    return choice6Score;
  }

  /**
   * Sets the score for choice 6.
   *
   * @param choice6Score the choice 6 score to set
   * @author mian
   */
  public void setChoice6Score(Integer choice6Score) {
    this.choice6Score = choice6Score;
  }

  /**
   * Gets the total score for the student.
   *
   * @return the student's total score
   * @author mian
   */
  public Integer getStudentTotalScore() {
    return studentTotalScore;
  }

  /**
   * Sets the total score for the student.
   *
   * @param studentTotalScore the student's total score to set
   * @author mian
   */
  public void setStudentTotalScore(Integer studentTotalScore) {
    this.studentTotalScore = studentTotalScore;
  }

  /**
   * Gets the calculation timestamp.
   *
   * @return the calculation timestamp
   * @author mian
   */
  public LocalDateTime getCalculationTimestamp() {
    return calculationTimestamp;
  }

  /**
   * Sets the calculation timestamp.
   *
   * @param calculationTimestamp the calculation timestamp to set
   * @author mian
   */
  public void setCalculationTimestamp(LocalDateTime calculationTimestamp) {
    this.calculationTimestamp = calculationTimestamp;
  }

  /**
   * Gets the overall fulfillment percentage.
   *
   * @return the overall fulfillment percentage
   * @author mian
   */
  public Double getOverallFulfillmentPercentage() {
    return overallFulfillmentPercentage;
  }

  /**
   * Sets the overall fulfillment percentage.
   *
   * @param overallFulfillmentPercentage the overall fulfillment percentage to set
   * @author mian
   */
  public void setOverallFulfillmentPercentage(Double overallFulfillmentPercentage) {
    this.overallFulfillmentPercentage = overallFulfillmentPercentage;
  }

  /**
   * Gets the total number of students.
   *
   * @return the total number of students
   * @author mian
   */
  public Integer getTotalStudents() {
    return totalStudents;
  }

  /**
   * Sets the total number of students.
   *
   * @param totalStudents the total number of students to set
   * @author mian
   */
  public void setTotalStudents(Integer totalStudents) {
    this.totalStudents = totalStudents;
  }

  /**
   * Gets the total score.
   *
   * @return the total score
   * @author mian
   */
  public Integer getTotalScore() {
    return totalScore;
  }

  /**
   * Sets the total score.
   *
   * @param totalScore the total score to set
   * @author mian
   */
  public void setTotalScore(Integer totalScore) {
    this.totalScore = totalScore;
  }

  /**
   * Gets the maximum possible score.
   *
   * @return the maximum possible score
   * @author mian
   */
  public Double getMaxPossibleScore() {
    return maxPossibleScore;
  }

  /**
   * Sets the maximum possible score.
   *
   * @param maxPossibleScore the maximum possible score to set
   * @author mian
   */
  public void setMaxPossibleScore(Double maxPossibleScore) {
    this.maxPossibleScore = maxPossibleScore;
  }

  /**
   * Checks if this FulfillmentScore is equal to another object.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   * @author mian
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FulfillmentScore that = (FulfillmentScore) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(studentId, that.studentId) &&
        Objects.equals(classRef, that.classRef) &&
        Objects.equals(firstName, that.firstName) &&
        Objects.equals(lastName, that.lastName) &&
        Objects.equals(choice1Score, that.choice1Score) &&
        Objects.equals(choice2Score, that.choice2Score) &&
        Objects.equals(choice3Score, that.choice3Score) &&
        Objects.equals(choice4Score, that.choice4Score) &&
        Objects.equals(choice5Score, that.choice5Score) &&
        Objects.equals(choice6Score, that.choice6Score) &&
        Objects.equals(studentTotalScore, that.studentTotalScore) &&
        Objects.equals(calculationTimestamp, that.calculationTimestamp) &&
        Objects.equals(overallFulfillmentPercentage, that.overallFulfillmentPercentage) &&
        Objects.equals(totalStudents, that.totalStudents) &&
        Objects.equals(totalScore, that.totalScore) &&
        Objects.equals(maxPossibleScore, that.maxPossibleScore);
  }

  /**
   * Generates a hash code for this FulfillmentScore.
   *
   * @return the hash code
   * @author mian
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, studentId, classRef, firstName, lastName,
        choice1Score, choice2Score, choice3Score, choice4Score, choice5Score, choice6Score,
        studentTotalScore, calculationTimestamp, overallFulfillmentPercentage,
        totalStudents, totalScore, maxPossibleScore);
  }

  /**
   * Returns a string representation of this FulfillmentScore.
   *
   * @return the string representation
   * @author mian
   */
  @Override
  public String toString() {
    return "FulfillmentScore{" +
        "id=" + id +
        ", studentId='" + studentId + '\'' +
        ", classRef='" + classRef + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", choice1Score=" + choice1Score +
        ", choice2Score=" + choice2Score +
        ", choice3Score=" + choice3Score +
        ", choice4Score=" + choice4Score +
        ", choice5Score=" + choice5Score +
        ", choice6Score=" + choice6Score +
        ", studentTotalScore=" + studentTotalScore +
        ", calculationTimestamp=" + calculationTimestamp +
        ", overallFulfillmentPercentage=" + overallFulfillmentPercentage +
        ", totalStudents=" + totalStudents +
        ", totalScore=" + totalScore +
        ", maxPossibleScore=" + maxPossibleScore +
        '}';
  }
}