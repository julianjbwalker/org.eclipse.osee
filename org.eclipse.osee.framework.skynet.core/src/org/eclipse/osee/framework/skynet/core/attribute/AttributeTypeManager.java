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
package org.eclipse.osee.framework.skynet.core.attribute;

import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTRIBUTE_BASE_TYPE_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTRIBUTE_PROVIDER_TYPE_TABLE;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_BASE_TYPE_ID_SEQ;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_PROVIDER_TYPE_ID_SEQ;
import static org.eclipse.osee.framework.db.connection.core.schema.SkynetDatabase.ATTR_TYPE_ID_SEQ;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.ConnectionHandlerStatement;
import org.eclipse.osee.framework.db.connection.DbUtil;
import org.eclipse.osee.framework.db.connection.core.query.Query;
import org.eclipse.osee.framework.db.connection.info.SQL3DataType;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.attribute.providers.IAttributeDataProvider;

/**
 * @author Ryan D. Brooks
 */
public class AttributeTypeManager {

   private static final String SELECT_ATTRIBUTE_TYPES =
         "SELECT * FROM osee_define_attribute_type aty1, osee_define_attr_base_type aby1, osee_define_attr_provider_type apy1 WHERE aty1.attr_base_type_id = aby1.attr_base_type_id AND aty1.attr_provider_type_id = apy1.attr_provider_type_id";
   private static final String INSERT_ATTRIBUTE_TYPE =
         "INSERT INTO osee_define_attribute_type (attr_type_id, attr_base_type_id, attr_provider_type_id, file_type_extension, namespace, name, default_value, validity_xml, min_occurence, max_occurence, tip_text) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
   private static final String INSERT_BASE_ATTRIBUTE_TYPE =
         "INSERT INTO osee_define_attr_base_type (attr_base_type_id, attribute_class) VALUES (?, ?)";
   private static final String INSERT_ATTRIBUTE_PROVIDER_TYPE =
         "INSERT INTO osee_define_attr_provider_type (attr_provider_type_id, attribute_provider_class) VALUES (?, ?)";
   private static final String SELECT_ATTRIBUTE_BASE_TYPE =
         "SELECT attr_base_type_id FROM " + ATTRIBUTE_BASE_TYPE_TABLE + " WHERE attribute_class = ?";
   private static final String SELECT_ATTRIBUTE_PROVIDER_TYPE =
         "SELECT attr_provider_type_id FROM " + ATTRIBUTE_PROVIDER_TYPE_TABLE + " WHERE attribute_provider_class = ?";

   private final HashMap<String, AttributeType> nameToTypeMap;
   private final HashMap<Integer, AttributeType> idToTypeMap;
   private static final AttributeTypeManager instance = new AttributeTypeManager();

   private AttributeTypeManager() {
      this.nameToTypeMap = new HashMap<String, AttributeType>();
      this.idToTypeMap = new HashMap<Integer, AttributeType>();
   }

   private static synchronized void ensurePopulated() throws SQLException {
      if (instance.idToTypeMap.size() == 0) {
         instance.populateCache();
      }
   }

   private void populateCache() throws SQLException {
      ConnectionHandlerStatement chStmt = null;

      try {
         chStmt = ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTE_TYPES);

         ResultSet rSet = chStmt.getRset();
         while (rSet.next()) {
            String baseClassString = rSet.getString("attribute_class");
            String baseProviderClassString = rSet.getString("attribute_provider_class");
            try {
               AttributeExtensionManager extensionManager = AttributeExtensionManager.getInstance();

               Class<? extends Attribute<?>> baseAttributeClass =
                     extensionManager.getAttributeClassFor(baseClassString);
               Class<? extends IAttributeDataProvider> providerAttributeClass =
                     extensionManager.getAttributeProviderClassFor(baseProviderClassString);

               AttributeType type =
                     new AttributeType(rSet.getInt("attr_type_id"), baseAttributeClass, providerAttributeClass,
                           rSet.getString("file_type_extension"), rSet.getString("namespace"), rSet.getString("name"),
                           rSet.getString("default_value"), rSet.getString("validity_xml"),
                           rSet.getInt("min_occurence"), rSet.getInt("max_occurence"), rSet.getString("tip_text"));
               cache(type);

            } catch (IllegalStateException ex) {
               SkynetActivator.getLogger().log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch (ClassNotFoundException ex) {
               SkynetActivator.getLogger().log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
         }
      } finally {
         DbUtil.close(chStmt);
      }
   }

   /**
    * use attribute validitiy to get attributes by branch instead
    * 
    * @return Returns all of the descriptors.
    * @throws Exception
    */
   @Deprecated
   public static Collection<AttributeType> getTypes(Branch branch) throws SQLException {
      ensurePopulated();
      return instance.idToTypeMap.values();
   }

   public static boolean typeExists(String namespace, String name) throws Exception {
      ensurePopulated();
      return instance.nameToTypeMap.get(namespace + name) != null;
   }

   /**
    * Returns the attribute type with the given name and namespace or throws an IllegalArgumentException if it does not
    * exist.
    * 
    * @param attrTypeId
    * @return
    * @throws SQLException
    * @throws IllegalArgumentException
    */
   public static AttributeType getType(String namespace, String name) throws SQLException {
      ensurePopulated();
      AttributeType attributeType = instance.nameToTypeMap.get(namespace + name);
      if (attributeType == null) {
         throw new IllegalArgumentException(
               "Attribute Type with namespace \"" + namespace + "\" and name \"" + name + "\" does not exist.");
      }
      return attributeType;
   }

   /**
    * Returns the attribute type with the given type id or throws an IllegalArgumentException if it does not exist.
    * 
    * @param attrTypeId
    * @return
    * @throws SQLException
    * @throws IllegalArgumentException
    */
   public static AttributeType getType(int attrTypeId) throws SQLException, IllegalArgumentException {
      ensurePopulated();
      AttributeType attributeType = instance.idToTypeMap.get(attrTypeId);
      if (attributeType == null) {

         throw new IllegalArgumentException("Attribute type: " + attrTypeId + " is not available.");
      }
      return attributeType;
   }

   /**
    * Returns the attribute type with the given name or throws an IllegalArgumentException if it does not exist.
    * 
    * @param attrTypeId
    * @return
    * @throws SQLException
    * @throws IllegalArgumentException
    */
   public static AttributeType getType(String name) throws SQLException {
      return getType("", name);
   }

   /**
    * Cache a newly created type.
    * 
    * @param attributeType
    * @throws SQLException
    */
   public void cache(AttributeType attributeType) throws SQLException {
      nameToTypeMap.put(attributeType.getNamespace() + attributeType.getName(), attributeType);
      idToTypeMap.put(attributeType.getAttrTypeId(), attributeType);
   }

   public static AttributeType createType(String attributeBaseType, String attributeProviderTypeName, String fileTypeExtension, String namespace, String name, String defaultValue, String validityXml, int minOccurrences, int maxOccurrences, String tipText) throws Exception {
      if (typeExists(namespace, name)) {
         return getType(namespace, name);
      }

      AttributeExtensionManager extensionManager = AttributeExtensionManager.getInstance();

      Class<? extends Attribute<?>> baseAttributeClass = extensionManager.getAttributeClassFor(attributeBaseType);
      Class<? extends IAttributeDataProvider> providerAttributeClass =
            extensionManager.getAttributeProviderClassFor(attributeProviderTypeName);

      int attrTypeId = Query.getNextSeqVal(ATTR_TYPE_ID_SEQ);
      int attrBaseTypeId = instance.getOrCreateAttributeBaseType(attributeBaseType);
      int attrProviderTypeId = instance.getOrCreateAttributeProviderType(attributeProviderTypeName);
      ConnectionHandler.runPreparedUpdate(INSERT_ATTRIBUTE_TYPE, SQL3DataType.INTEGER, attrTypeId,
            SQL3DataType.INTEGER, attrBaseTypeId, SQL3DataType.INTEGER, attrProviderTypeId, SQL3DataType.VARCHAR,
            fileTypeExtension, SQL3DataType.VARCHAR, namespace, SQL3DataType.VARCHAR, name, SQL3DataType.VARCHAR,
            defaultValue, SQL3DataType.VARCHAR, validityXml, SQL3DataType.INTEGER, minOccurrences,
            SQL3DataType.INTEGER, maxOccurrences, SQL3DataType.VARCHAR, tipText);

      AttributeType descriptor =
            new AttributeType(attrTypeId, baseAttributeClass, providerAttributeClass, fileTypeExtension, namespace,
                  name, defaultValue, validityXml, minOccurrences, maxOccurrences, tipText);
      instance.cache(descriptor);
      return descriptor;
   }

   private int getOrCreateAttributeProviderType(String attrProviderExtension) throws SQLException {
      int attrBaseTypeId = -1;
      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt =
               ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTE_PROVIDER_TYPE, SQL3DataType.VARCHAR,
                     attrProviderExtension);
         ResultSet rSet = chStmt.getRset();
         if (rSet.next()) {
            attrBaseTypeId = rSet.getInt("attr_provider_type_id");
         } else {
            attrBaseTypeId = Query.getNextSeqVal(ATTR_PROVIDER_TYPE_ID_SEQ);

            ConnectionHandler.runPreparedUpdate(INSERT_ATTRIBUTE_PROVIDER_TYPE, SQL3DataType.INTEGER, attrBaseTypeId,
                  SQL3DataType.VARCHAR, attrProviderExtension);
         }
      } finally {
         DbUtil.close(chStmt);
      }
      return attrBaseTypeId;
   }

   private int getOrCreateAttributeBaseType(String attrBaseExtension) throws SQLException {
      int attrBaseTypeId = -1;
      ConnectionHandlerStatement chStmt = null;
      try {
         chStmt =
               ConnectionHandler.runPreparedQuery(SELECT_ATTRIBUTE_BASE_TYPE, SQL3DataType.VARCHAR, attrBaseExtension);
         ResultSet rSet = chStmt.getRset();
         if (rSet.next()) {
            attrBaseTypeId = rSet.getInt("attr_base_type_id");
         } else {
            attrBaseTypeId = Query.getNextSeqVal(ATTR_BASE_TYPE_ID_SEQ);

            ConnectionHandler.runPreparedUpdate(INSERT_BASE_ATTRIBUTE_TYPE, SQL3DataType.INTEGER, attrBaseTypeId,
                  SQL3DataType.VARCHAR, attrBaseExtension);
         }
      } finally {
         DbUtil.close(chStmt);
      }

      return attrBaseTypeId;
   }
}