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
public class Rental implements RentalDTO {
    private int availableInstrumentAmount;
    private String instrumentName;
    private String intsrumentType;
    private int rentalCost;
    private int instrumentID;

    public Rental(String instrumentName, String intsrumentType, int rentalCost){
        this.instrumentName = instrumentName;
        this.intsrumentType = intsrumentType;
        this.rentalCost = rentalCost;
    }

    public Rental(String instrumentName, int availableInstrumentAmount, int instrumentID){
        this.instrumentID = instrumentID;
        this.instrumentName = instrumentName;
        this.availableInstrumentAmount = availableInstrumentAmount;
    }
    public String getInstrumentName() {
        return instrumentName;
    }

    public int getRentalCost() {
        return rentalCost;
    }

    public String getIntsrumentType() {
        return intsrumentType;
    }

    public int getAvailableInstrumentAmount(){ return availableInstrumentAmount;}

    public int getInstrumentID() {
        return instrumentID;
    }

    public void newRental() throws  RejectedException{
        if (availableInstrumentAmount <= 0) {
            throw new RejectedException("Can not rent, no available of this brand");
        }
    }

    /**
     * @return A string representation of all fields in this object.
     */
    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.append("Rental: [");
        stringRepresentation.append("instrument brand: ");
        stringRepresentation.append(instrumentName);
        stringRepresentation.append(", instrument type ");
        stringRepresentation.append(intsrumentType);
        stringRepresentation.append(", cost: ");
        stringRepresentation.append(rentalCost);
        stringRepresentation.append("]");
        return stringRepresentation.toString();
    }
}
