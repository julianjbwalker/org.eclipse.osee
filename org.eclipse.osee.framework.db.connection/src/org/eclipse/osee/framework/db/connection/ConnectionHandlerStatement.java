/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.db.connection;

import java.io.InputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * Statment object created by the ConnectionHandler. It contains: <li>ResultSet <li>Statement
 * 
 * @author Jeff C. Phillips
 */
public class ConnectionHandlerStatement {
   private ResultSet rSet;
   private Statement statement;

   public boolean next() throws OseeDataStoreException {
      if (rSet != null) {
         try {
            return rSet.next();
         } catch (SQLException ex) {
            throw new OseeDataStoreException(ex);
         }
      }
      return false;
   }

   /**
    * use the ConnectionHandlerStatement directly for getX() calls and next()
    * 
    * @return Returns the rset.
    */
   @Deprecated
   public ResultSet getRset() {
      return rSet;
   }

   /**
    * @param rset The rset to set.
    */
   public void setRset(ResultSet rset) {
      this.rSet = rset;
   }

   /**
    * @return Returns the statement.
    */
   public Statement getStatement() {
      return statement;
   }

   /**
    * @param statement The statement to set.
    */
   public void setStatement(Statement statement) {
      this.statement = statement;
   }

   public void close() {
      try {
         if (rSet != null) {
            rSet.close();
         }
         if (statement != null) {
            statement.close();
         }
      } catch (SQLException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public InputStream getBinaryStream(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getBinaryStream(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public String getString(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getString(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public float getFloat(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getFloat(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public long getLong(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getLong(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public int getInt(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getInt(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   /**
    * should not be used by application code because it is less readable than using the column name
    * 
    * @param columnIndex
    * @return
    * @throws OseeDataStoreException
    */
   int getInt(int columnIndex) throws OseeDataStoreException {
      try {
         return rSet.getInt(columnIndex);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   /**
    * should not be used by application code because it is less readable than using the column name
    * 
    * @param columnIndex
    * @return
    * @throws OseeDataStoreException
    */
   long getLong(int columnIndex) throws OseeDataStoreException {
      try {
         return rSet.getLong(columnIndex);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   /**
    * should not be used by application code because it is less readable than using the column name
    * 
    * @param columnIndex
    * @return
    * @throws OseeDataStoreException
    */
   String getString(int columnIndex) throws OseeDataStoreException {
      try {
         return rSet.getString(columnIndex);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public Timestamp getTimestamp(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getTimestamp(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public double getDouble(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getDouble(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public Date getDate(String columnName) throws OseeDataStoreException {
      try {
         return rSet.getDate(columnName);
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }

   public boolean wasNull() throws OseeDataStoreException {
      try {
         return rSet.wasNull();
      } catch (SQLException ex) {
         throw new OseeDataStoreException(ex);
      }
   }
}