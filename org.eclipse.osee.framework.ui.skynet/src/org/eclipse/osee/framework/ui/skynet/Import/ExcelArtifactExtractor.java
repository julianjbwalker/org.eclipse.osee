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
package org.eclipse.osee.framework.ui.skynet.Import;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.type.DoubleKeyHashMap;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelSaxHandler;
import org.eclipse.osee.framework.jdk.core.util.io.xml.RowProcessor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Ryan D. Brooks
 */
public class ExcelArtifactExtractor extends AbstractArtifactExtractor implements RowProcessor {
   private static final String description = "Extract each row as an artifact - header <section #, atrribute1, atrribute2 ...>";
   private ExcelSaxHandler excelHandler;
   private String[] headerRow;
   private ArtifactSubtypeDescriptor primaryDescriptor;
   private boolean importingRelations;
   private boolean reuseArtifacts;
   private AttributeImportType[] types;
   private int rowCount;
   private DoubleKeyHashMap<String, Integer, RoughArtifact> relationHelper;
   private static Pattern pattern = Pattern.compile("(\\d*);(.*)");
   private static Matcher matches = pattern.matcher("");

   public static String getDescription() {
      return description;
   }

   public ExcelArtifactExtractor(Branch branch, boolean reuseArtifacts) {
      super(branch);
      this.reuseArtifacts = reuseArtifacts;
      relationHelper = new DoubleKeyHashMap<String, Integer, RoughArtifact>();
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.define.artifact.Import.RowProcessor#processHeaderRow(java.lang.String[])
    */
   public void processHeaderRow(String[] headerRow) {
      rowCount++;
      this.headerRow = headerRow.clone();
      for (int i = 0; i < this.headerRow.length; i++) {
         if (headerRow[i] != null && headerRow[i].trim().length() == 0) {
            this.headerRow[i] = null;
         }
      }
      types = new AttributeImportType[headerRow.length];
      for (int i = 0; i < types.length; i++) {
         types[i] = AttributeImportType.NONE;
      }
   }

   /**
    * import Artifacts
    * 
    * @param row
    */
   public void processRow(String[] row) {
      rowCount++;
      System.out.println("Row " + rowCount);
      if (importingRelations) {
         String guida = null;
         String guidb = null;
         try {
            guida = getGuid(row[1]);
            guidb = getGuid(row[2]);
         } catch (Exception ex) {
            throw new IllegalStateException(ex);
         }

         if (guida == null || guidb == null) {
            System.out.println("we failed to add a relation");
         }
         addRoughRelation(new RoughRelation(row[0], guida, guidb, row[5], Integer.parseInt(row[3]),
               Integer.parseInt(row[4])));
         return;
      }

      RoughArtifact roughArtifact = new RoughArtifact();
      roughArtifact.setHeadingDescriptor(primaryDescriptor);
      roughArtifact.setPrimaryDescriptor(primaryDescriptor);
      for (int i = 0; i < row.length; i++) {
         if (headerRow[i] == null) continue;
         if (headerRow[i].equalsIgnoreCase("Outline Number")) {
            if (row[i] == null) {
               throw new IllegalArgumentException("Outline Number must not be blank");
            }
            roughArtifact.setSectionNumber(row[i]);
         } else if (headerRow[i].equalsIgnoreCase("GUID")) {
            roughArtifact.setGuid(row[i]);
         } else if (headerRow[i].equalsIgnoreCase("Human Readable Id")) {
            roughArtifact.setHumandReadableId(row[i]);
         } else {
            roughArtifact.addAttribute(headerRow[i], row[i], types[i]);
         }
      }
      roughArtifact = returnUniqueInstanceAndAddToList(roughArtifact);

      relationHelper.put(primaryDescriptor.getName(), new Integer(rowCount), roughArtifact);
   }

   private RoughArtifact returnUniqueInstanceAndAddToList(RoughArtifact artifact) {
      if (reuseArtifacts) {
         List<RoughArtifact> arts = getRoughArtifacts();
         for (RoughArtifact art : arts) {
            if (art.isEqual(artifact)) {
               return art;
            }
         }
      }
      addRoughArtifact(artifact);
      return artifact;
   }

   /**
    * @param string
    * @throws Exception
    * @throws SQLException
    */
   private String getGuid(String string) throws Exception {
      if (GUID.isValid(string)) {//it may be real guid
         return string;
      }
      matches.reset(string);
      if (matches.matches()) {
         Integer row = Integer.parseInt(matches.group(1));
         String sheet = matches.group(2);
         RoughArtifact art = relationHelper.get(sheet, row);
         Artifact real = art.getReal(branch, null, reuseArtifacts);
         String guid = real.getGuid();
         return guid;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see osee.define.artifact.Import.ArtifactExtractor#discoverArtifactAndRelationData(java.io.File)
    */
   public void discoverArtifactAndRelationData(File artifactsFile) throws Exception {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      excelHandler = new ExcelSaxHandler(this, true);
      xmlReader.setContentHandler(excelHandler);
      xmlReader.parse(new InputSource(new InputStreamReader(new FileInputStream(artifactsFile), "UTF-8")));
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.define.artifact.Import.RowProcessor#processEmptyRow()
    */
   public void processEmptyRow() {
      rowCount++;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.define.artifact.Import.RowProcessor#processCommentRow(java.lang.String[])
    */
   public void processCommentRow(String[] row) {
      rowCount++;
      if (reuseArtifacts) {
         for (int i = 0; i < row.length; i++) {
            if (row[i] != null) {
               try {
                  types[i] = AttributeImportType.valueOf(row[i]);
               } catch (Throwable th) {
                  types[i] = AttributeImportType.NONE;
               }
            } else {
               types[i] = AttributeImportType.NONE;
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.define.artifact.Import.RowProcessor#reachedEndOfWorksheet()
    */
   public void reachedEndOfWorksheet() {
   }

   /* (non-Javadoc)
    * @see osee.define.artifact.Import.RowProcessor#detectedTotalRowCount(int)
    */
   public void detectedRowAndColumnCounts(int rowCount, int columnCount) {
   }

   /* (non-Javadoc)
    * @see osee.define.artifact.Import.RowProcessor#foundStartOfWorksheet(java.lang.String)
    */
   public void foundStartOfWorksheet(String sheetName) {
      rowCount = 0;
      try {
         if (sheetName.startsWith("Aspect Impact Implement Complet")) {
            sheetName = "Aspect Impact Implement Complete State";
         }
         if (sheetName.startsWith("Aspect Impact Analyze Complete")) {
            sheetName = "Aspect Impact Analyze Complete State";
         }
         if (sheetName.startsWith("Product Impact Implement Comple")) {
            sheetName = "Product Impact Implement Complete State";
         }
         if (sheetName.startsWith("Product Impact Analyze Complete")) {
            sheetName = "Product Impact Analyze Complete State";
         }

         if (sheetName.equals("relations")) {
            importingRelations = true;
            return;
         }
         primaryDescriptor = configurationPersistenceManager.getArtifactSubtypeDescriptor(sheetName, branch);
         if (primaryDescriptor == null) {
            throw new IllegalArgumentException("The sheet name: " + sheetName + " is not a valid artifact type name.");
         }
      } catch (SQLException ex) {
         throw new IllegalArgumentException(
               "The sheet name: " + sheetName + " is not a valid artifact type name: " + ex.getMessage());
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.Import.ArtifactExtractor#getFileFilter()
    */
   public FileFilter getFileFilter() {
      return new FileFilter() {
         public boolean accept(File file) {
            return file.isDirectory() || (file.isFile() && file.getName().endsWith(".xml"));
         }
      };
   }
}