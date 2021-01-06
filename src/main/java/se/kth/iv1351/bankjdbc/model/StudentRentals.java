/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Leif Lindbäck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so,subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.kth.iv1351.bankjdbc.model;
public class StudentRentals implements StudentRentalsDTO {
    private int studentID;
    private int numberOfRentals;
    private int rentalID;

    public StudentRentals(int studentID, int numberOfRentals){
        this.studentID = studentID;
        this.numberOfRentals = numberOfRentals;
    }

    public StudentRentals(int rentalID){
        this.rentalID = rentalID;
    }

    public int getNumberOfRentals() {
        return numberOfRentals;
    }

    public int getStudentID() {
        return studentID;
    }

    public void newRental() throws RejectedException{
        if (numberOfRentals >= 2) {
            throw new RejectedException("Can not rent more then 2 at the same time");
        }
        numberOfRentals ++;
    }

    /**
     * @return A string representation of all fields in this object.
     */
    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.append("Student Rentals: [");
        stringRepresentation.append("Student ID: ");
        stringRepresentation.append(studentID);
        stringRepresentation.append(", number of rentals ");
        stringRepresentation.append(numberOfRentals);
        stringRepresentation.append("]");
        return stringRepresentation.toString();
    }
}
