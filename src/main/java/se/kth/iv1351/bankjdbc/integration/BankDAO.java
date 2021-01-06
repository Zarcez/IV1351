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

package se.kth.iv1351.bankjdbc.integration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import se.kth.iv1351.bankjdbc.model.Account;
import se.kth.iv1351.bankjdbc.model.AccountDTO;
import se.kth.iv1351.bankjdbc.model.Rental;
import se.kth.iv1351.bankjdbc.model.StudentRentals;

/**
 * This data access object (DAO) encapsulates all database calls in the bank
 * application. No code outside this class shall have any knowledge about the
 * database.
 */
public class BankDAO {
    private static final String RENTING_TABLE_NAME = "renting_instrument";
    private static final String RENTING_PK = "renting_id";
    private static final String RENTING_AVAILABLE_AMOUNT = "available_instrument_amount";
    private static final String RENTING_INSTRUMENT_NAME = "instrument_name";
    private static final String RENTING_INSTRUMENT_TYPE = "intsrument_type";
    private static final String RENTING_RENTAL_COST = "rental_cost";

    private static final String RENTED_TABLE_NAME = "rented_instrument";
    private static final String RENTED_INSTRUMENT_ID = "instrument_id";
    private static final String RENTED_STUDENT_ID = "student_id";
    private static final String RENTED_DATE = "date";
    private static final String RENTED_CURRENTLY_RENTING = "currently_renting";
    private static final String RENTED_PK = "rented_id";

    private static final String CURRENT_DATE = "CURRENT_DATE";


    private static final String HOLDER_TABLE_NAME = "holder";
    private static final String HOLDER_PK_COLUMN_NAME = "holder_id";
    private static final String HOLDER_COLUMN_NAME = "name";
    private static final String ACCT_TABLE_NAME = "account";
    private static final String ACCT_NO_COLUMN_NAME = "account_no";
    private static final String BALANCE_COLUMN_NAME = "balance";
    private static final String HOLDER_FK_COLUMN_NAME = HOLDER_PK_COLUMN_NAME;

    private Connection connection;
    private PreparedStatement createHolderStmt;
    private PreparedStatement findHolderPKStmt;
    private PreparedStatement createAccountStmt;
    private PreparedStatement findAccountByNameStmt;
    private PreparedStatement findAccountByAcctNoStmt;
    private PreparedStatement findAllAccountsStmt;
    private PreparedStatement deleteAccountStmt;
    private PreparedStatement changeBalanceStmt;

    private PreparedStatement findRentalListType;
    private PreparedStatement findRentalNumber;
    private PreparedStatement findRentalName;
    private PreparedStatement createRentalStmt;
    private PreparedStatement changeRentalAmountStmt;
    private PreparedStatement findRentalIDStmt;
    private PreparedStatement closeRentalStmt;
    private PreparedStatement findRentalIDNameStmt;

    /**
     * Constructs a new DAO object connected to the bank database.
     */
    public BankDAO() throws BankDBException {
        try {
            connectToBankDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            throw new BankDBException("Could not connect to datasource.", exception);
        }
    }

    /**
     * Creates a new account.
     *
     * @param account The account to create.
     * @throws BankDBException If failed to create the specified account.
     */
    public void createAccount(AccountDTO account) throws BankDBException {
        String failureMsg = "Could not create the account: " + account;
        int updatedRows = 0;
        try {
            int holderPK = findHolderPKByName(account.getHolderName());
            if (holderPK == 0) {
                createHolderStmt.setString(1, account.getHolderName());
                updatedRows = createHolderStmt.executeUpdate();
                if (updatedRows != 1) {
                    handleException(failureMsg, null);
                }
                holderPK = findHolderPKByName(account.getHolderName());
            }

            createAccountStmt.setInt(1, createAccountNo());
            createAccountStmt.setInt(2, account.getBalance());
            createAccountStmt.setInt(3, holderPK);
            updatedRows = createAccountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }

            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Searches for the account with the specified account number.
     *
     * @param acctNo The account number.
     * @return The account with the specified account number, or <code>null</code> if 
     *         there is no such account.
     * @throws BankDBException If failed to search for the account.
     */
    public Account findAccountByAcctNo(String acctNo) throws BankDBException {
        String failureMsg = "Could not search for specified account.";
        ResultSet result = null;
        try {
            findAccountByAcctNoStmt.setString(1, acctNo);
            result = findAccountByAcctNoStmt.executeQuery();
            if (result.next()) {
                return new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                   result.getString(HOLDER_COLUMN_NAME),
                                   result.getInt(BALANCE_COLUMN_NAME));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return null;
    }

    /**
     * Searches for all accounts whose holder has the specified name.
     *
     * @param holderName The account holder's name
     * @return A list with all accounts whose holder has the specified name, 
     *         the list is empty if there are no such account.
     * @throws BankDBException If failed to search for accounts.
     */
    public List<Account> findAccountsByHolder(String holderName) throws BankDBException {
        String failureMsg = "Could not search for specified accounts.";
        ResultSet result = null;
        List<Account> accounts = new ArrayList<>();
        try {
            findAccountByNameStmt.setString(1, holderName);
            result = findAccountByNameStmt.executeQuery();
            while (result.next()) {
                accounts.add(new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                         result.getString(HOLDER_COLUMN_NAME),
                                         result.getInt(BALANCE_COLUMN_NAME)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return accounts;
    }

    public List<Rental> findRentalByType(String type) throws BankDBException {
        String failureMsg = "Could not find any available rentals.";
        ResultSet result = null;
        List<Rental> rentals = new ArrayList<>();
        try {
            findRentalListType.setString(1, type);
            result = findRentalListType.executeQuery();
            while (result.next()) {
                rentals.add(new Rental(result.getString(RENTING_INSTRUMENT_NAME),
                        type,
                        result.getInt(RENTING_RENTAL_COST)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return rentals;
    }

    public StudentRentals findRentalNumberTot(int id) throws BankDBException {
        String failureMsg = "Could not find any available rentals.";
        ResultSet result = null;
        try {
            findRentalNumber.setInt(1, id);
            result = findRentalNumber.executeQuery();
            if (result.next()) {
                return (new StudentRentals(
                        id,result.getInt(1)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return null;
    }

    public Rental findRental(String name) throws BankDBException{
        String failureMsg = "Could not find any available rentals.";
        ResultSet result = null;
        try {
            findRentalName.setString(1, name);
            result = findRentalName.executeQuery();
            //ResultSetMetaData rsmd = result.getMetaData();
            //String name2 = rsmd.getColumnName(1);
            //System.out.println(name2);
            if (result.next()) {
                return (new Rental(result.getString(RENTING_INSTRUMENT_NAME),
                        result.getInt(RENTING_AVAILABLE_AMOUNT),
                        result.getInt(RENTING_PK)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return null;
    }

    public void newRental(int studentID, Rental rental) throws BankDBException {
        String failureMsg = "Could not make the new rental";
        int updatedRows = 0;
        try {
            createRentalStmt.setInt(1,rental.getInstrumentID());
            createRentalStmt.setInt(2,studentID);
            //createRentalStmt.setString(3,CURRENT_DATE);
            updatedRows = createRentalStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            changeRentalAmountStmt.setInt(1,rental.getAvailableInstrumentAmount()-1);
            System.out.println(rental.getInstrumentName());

            changeRentalAmountStmt.setString(2,rental.getInstrumentName());
            updatedRows = changeRentalAmountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }

            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    public  int findRentalID(int instrumentID, int studentId) throws BankDBException{
        String failureMsg = "Could not find any rentals for that brand and ID.";
        ResultSet result = null;
        int id = -1;
        try {
            findRentalIDStmt.setInt(1, studentId);
            findRentalIDStmt.setInt(2, instrumentID);
            result = findRentalIDStmt.executeQuery();
            if (result.next()) {
                id = result.getInt(RENTED_PK);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return id;
    }

    public void clostRental(int studentID, int rentalID, Rental rental) throws BankDBException {
        String failureMsg = "Could not close the rental";
        int updatedRows = 0;
        try {
            closeRentalStmt.setInt(1,rentalID);
            closeRentalStmt.setInt(2,studentID);
            updatedRows = closeRentalStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            changeRentalAmountStmt.setInt(1,rental.getAvailableInstrumentAmount()+1);
            changeRentalAmountStmt.setString(2,rental.getInstrumentName());
            updatedRows = changeRentalAmountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }

            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }



    /**
     * Retrieves all existing accounts.
     *
     * @return A list with all existing accounts. The list is empty if there are no
     *         accounts.
     * @throws BankDBException If failed to search for accounts.
     */
    public List<Account> findAllAccounts() throws BankDBException {
        String failureMsg = "Could not list accounts.";
        List<Account> accounts = new ArrayList<>();
        try (ResultSet result = findAllAccountsStmt.executeQuery()) {
            while (result.next()) {
                accounts.add(new Account(result.getString(ACCT_NO_COLUMN_NAME),
                                         result.getString(HOLDER_COLUMN_NAME),
                                         result.getInt(BALANCE_COLUMN_NAME)));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
        return accounts;
    }

    /**
     * Changes the balance of the account with the number of the specified
     * <code>AccountDTO</code> object. The balance is set to the value in the specified
     * <code>AccountDTO</code>.
     *
     * @param account The account to update.
     * @throws BankDBException If unable to update the specified account.
     */
    public void updateAccount(AccountDTO account) throws BankDBException {
        String failureMsg = "Could not update the account: " + account;
        try {
            changeBalanceStmt.setInt(1, account.getBalance());
            changeBalanceStmt.setString(2, account.getAccountNo());
            int updatedRows = changeBalanceStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Deletes the account with the specified account number.
     *
     * @param acctNo The account to delete.
     * @throws BankDBException If unable to delete the specified account.
     */
    public void deleteAccount(String acctNo) throws BankDBException {
        String failureMsg = "Could not delete account: " + acctNo;
        try {
            deleteAccountStmt.setString(1, acctNo);
            int updatedRows = deleteAccountStmt.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    private void connectToBankDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/data",
                                                 "postgres", "example");
        // connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bankdb",
        //                                          "root", "javajava");
        connection.setAutoCommit(false);
    }

    private void prepareStatements() throws SQLException {
        createHolderStmt = connection.prepareStatement("INSERT INTO " + HOLDER_TABLE_NAME
            + "(" + HOLDER_COLUMN_NAME + ") VALUES (?)");

        createAccountStmt = connection.prepareStatement("INSERT INTO " + ACCT_TABLE_NAME
            + "(" + ACCT_NO_COLUMN_NAME + ", " + BALANCE_COLUMN_NAME + ", "
            + HOLDER_FK_COLUMN_NAME + ") VALUES (?, ?, ?)");



        findRentalListType = connection.prepareStatement("SELECT rg."+ RENTING_INSTRUMENT_NAME+", rg." +
                RENTING_RENTAL_COST + " from " + RENTING_TABLE_NAME+ " rg "+ "WHERE rg." + RENTING_AVAILABLE_AMOUNT +
                " > 0 AND "+RENTING_INSTRUMENT_TYPE + " = ? "
        );

        findRentalName = connection.prepareStatement("SELECT rg."+ RENTING_INSTRUMENT_NAME+", rg." +
                RENTING_AVAILABLE_AMOUNT +", rg." +RENTING_PK + " from " + RENTING_TABLE_NAME+ " rg "+
                "WHERE rg."+RENTING_INSTRUMENT_NAME + " = ? "
        );

        findRentalIDNameStmt = connection.prepareStatement("SELECT "+ RENTING_PK
                +" from " + RENTING_TABLE_NAME + " WHERE rg."+RENTING_INSTRUMENT_NAME + " = ? "
        );

        changeRentalAmountStmt = connection.prepareStatement("UPDATE " + RENTING_TABLE_NAME
                + " SET " + RENTING_AVAILABLE_AMOUNT + " = ? WHERE " + RENTING_INSTRUMENT_NAME + " = ? ");

        closeRentalStmt = connection.prepareStatement("UPDATE " + RENTED_TABLE_NAME
                + " SET " + RENTED_CURRENTLY_RENTING + " = '0' WHERE " + RENTED_PK + " = ? "
                + " AND "+ RENTED_STUDENT_ID + " = ?");

        findRentalIDStmt = connection.prepareStatement("SELECT " + RENTED_PK + " FROM " + RENTED_TABLE_NAME +
                " WHERE " + RENTED_STUDENT_ID + " = ? " + " AND " + RENTED_INSTRUMENT_ID + " = ?"
                + " AND " + RENTED_CURRENTLY_RENTING + " = '1'");

        findRentalNumber = connection.prepareStatement("SELECT COUNT(*) as count" + " FROM " +
                RENTED_TABLE_NAME +" rd " + " WHERE rd." + RENTED_STUDENT_ID +" = ?"
                + " AND " + RENTED_CURRENTLY_RENTING+ " = '1'");

        createRentalStmt = connection.prepareStatement("INSERT INTO " + RENTED_TABLE_NAME
                + "(" + RENTED_INSTRUMENT_ID + ", " + RENTED_STUDENT_ID + ", " + RENTED_DATE + ", "
                +  RENTED_CURRENTLY_RENTING  + ") VALUES (?, ?, CURRENT_DATE, '1')");


        findHolderPKStmt = connection.prepareStatement("SELECT " + HOLDER_PK_COLUMN_NAME
            + " FROM " + HOLDER_TABLE_NAME + " WHERE " + HOLDER_COLUMN_NAME + " = ?");

        findAccountByAcctNoStmt = connection.prepareStatement("SELECT a." + ACCT_NO_COLUMN_NAME
            + ", a." + BALANCE_COLUMN_NAME + ", h." + HOLDER_COLUMN_NAME + " from "
            + ACCT_TABLE_NAME + " a INNER JOIN " + HOLDER_TABLE_NAME + " h ON a."
            + HOLDER_FK_COLUMN_NAME + " = h." + HOLDER_PK_COLUMN_NAME + " WHERE a."
            + ACCT_NO_COLUMN_NAME + " = ?");

        findAccountByNameStmt = connection.prepareStatement("SELECT a." + ACCT_NO_COLUMN_NAME
            + ", a." + BALANCE_COLUMN_NAME + ", h." + HOLDER_COLUMN_NAME + " from "
            + ACCT_TABLE_NAME + " a INNER JOIN "
            + HOLDER_TABLE_NAME + " h ON a." + HOLDER_FK_COLUMN_NAME
            + " = h." + HOLDER_PK_COLUMN_NAME + " WHERE h." + HOLDER_COLUMN_NAME + " = ?");

        findAllAccountsStmt = connection.prepareStatement("SELECT h." + HOLDER_COLUMN_NAME
            + ", a." + ACCT_NO_COLUMN_NAME + ", a." + BALANCE_COLUMN_NAME + " FROM "
            + HOLDER_TABLE_NAME + " h INNER JOIN " + ACCT_TABLE_NAME + " a ON a."
            + HOLDER_FK_COLUMN_NAME + " = h." + HOLDER_PK_COLUMN_NAME);

        changeBalanceStmt = connection.prepareStatement("UPDATE " + ACCT_TABLE_NAME
            + " SET " + BALANCE_COLUMN_NAME + " = ? WHERE " + ACCT_NO_COLUMN_NAME + " = ? ");

        deleteAccountStmt = connection.prepareStatement("DELETE FROM " + ACCT_TABLE_NAME
            + " WHERE " + ACCT_NO_COLUMN_NAME + " = ?");
    }
    private void handleException(String failureMsg, Exception cause) throws BankDBException {
        String completeFailureMsg = failureMsg;
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            completeFailureMsg = completeFailureMsg + 
            ". Also failed to rollback transaction because of: " + rollbackExc.getMessage();
        }

        if (cause != null) {
            throw new BankDBException(failureMsg, cause);
        } else {
            throw new BankDBException(failureMsg);
        }
    }

    private void closeResultSet(String failureMsg, ResultSet result) throws BankDBException {
        try {
            result.close();
        } catch (Exception e) {
            throw new BankDBException(failureMsg + " Could not close result set.", e);
        }
    }

    private int createAccountNo() {
        return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
    }

    private int findHolderPKByName(String holderName) throws SQLException {
        ResultSet result = null;
        findHolderPKStmt.setString(1, holderName);
        result = findHolderPKStmt.executeQuery();
        if (result.next()) {
            return result.getInt(HOLDER_PK_COLUMN_NAME);
        }
        return 0;
    }
}
