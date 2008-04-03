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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.skynet.core.relation.RelationSide.DEFAULT_HIERARCHICAL__CHILD;
import static org.eclipse.osee.framework.skynet.core.relation.RelationSide.DEFAULT_HIERARCHICAL__PARENT;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.PersistenceMemo;
import org.eclipse.osee.framework.jdk.core.util.PersistenceObject;
import org.eclipse.osee.framework.messaging.event.skynet.event.SkynetAttributeChange;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.access.PermissionEnum;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent.ModType;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.ArtifactAnnotation;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.AttributeAnnotationManager;
import org.eclipse.osee.framework.skynet.core.artifact.annotation.IArtifactAnnotation;
import org.eclipse.osee.framework.skynet.core.artifact.factory.IArtifactFactory;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactHridSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.FromArtifactsSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.InRelationSearch;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactSubtypeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.ConfigurationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeDescriptor;
import org.eclipse.osee.framework.skynet.core.attribute.DynamicAttributeManager;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.relation.IRelationEnumeration;
import org.eclipse.osee.framework.skynet.core.relation.IRelationLink;
import org.eclipse.osee.framework.skynet.core.relation.IRelationType;
import org.eclipse.osee.framework.skynet.core.relation.LinkManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationLinkGroup;
import org.eclipse.osee.framework.skynet.core.relation.RelationPersistenceManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationSide;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionIdManager;
import org.eclipse.osee.framework.skynet.core.util.Requirements;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class Artifact implements PersistenceObject, IAdaptable, Comparable<Artifact> {
   public static final String UNNAMED = "Unnamed";
   public static final String BEFORE_GUID_STRING = "/BeforeGUID/PrePend";
   public static final String AFTER_GUID_STRING = "/AfterGUID";
   public static final Artifact[] EMPTY_ARRAY = new Artifact[0];
   protected static final ArtifactPersistenceManager artifactManager = ArtifactPersistenceManager.getInstance();
   protected static final ConfigurationPersistenceManager configurationManager =
         ConfigurationPersistenceManager.getInstance();
   protected static final RelationPersistenceManager relationManager = RelationPersistenceManager.getInstance();
   protected static final BranchPersistenceManager branchManager = BranchPersistenceManager.getInstance();
   private static final AccessControlManager accessManager = AccessControlManager.getInstance();
   private static int count = 0;
   public final int aaaSerialId = count++;
   @SuppressWarnings("unused")
   private final Date birthTime = new Date();
   @SuppressWarnings("unused")
   private final Exception birthPlace = new Exception();
   private final Branch branch;
   private final String guid;
   protected boolean deleteCheckOveride;
   protected boolean dirty;
   protected boolean inTransaction;
   private String artifactTypeName;
   private Collection<DynamicAttributeManager> attributeManagers;
   private boolean deleted;
   private ArtifactSubtypeDescriptor descriptor;
   private String humanReadableId;
   private LinkManager linkManager;
   private boolean initializingAttributes;
   private ArtifactPersistenceMemo memo;
   private IArtifactFactory parentFactory;
   private int deletionTransactionId;

   // TODO refactor annotationMgr to another class
   private AttributeAnnotationManager annotationMgr;

   protected Artifact(IArtifactFactory parentFactory, String guid, String humanReadableId, Branch branch) throws SQLException {

      // Make sure that the stack trace is available at debug time
      this.birthPlace.getStackTrace();

      if (guid == null) {
         this.guid = GUID.generateGuidStr();
      } else {
         this.guid = guid;
      }

      if (humanReadableId == null) {
         rollHumanReadableId();
      } else {
         this.humanReadableId = humanReadableId;
      }

      this.parentFactory = parentFactory;
      this.attributeManagers = null;
      this.branch = branch;
      this.dirty = true;
      this.inTransaction = false;
      this.deleted = false;
      this.memo = null;
      this.deleteCheckOveride = false;
      this.initializingAttributes = false;
   }

   public boolean isInDb() {
      return (memo != null);
   }

   public boolean isAnnotation(ArtifactAnnotation.Type type) {
      for (ArtifactAnnotation notify : getAnnotations()) {
         if (notify.getType() == type) return true;
      }
      return false;
   }

   public Set<ArtifactAnnotation> getAnnotations() {
      Set<ArtifactAnnotation> annotations = new HashSet<ArtifactAnnotation>();
      for (IArtifactAnnotation annotation : getAnnotationExtensions()) {
         annotation.getAnnotations(this, annotations);
      }
      return annotations;
   }

   public ArtifactAnnotation.Type getMainAnnotationType() {
      if (isAnnotation(ArtifactAnnotation.Type.Error))
         return ArtifactAnnotation.Type.Error;
      else if (isAnnotation(ArtifactAnnotation.Type.Warning))
         return ArtifactAnnotation.Type.Warning;
      else if (isAnnotation(ArtifactAnnotation.Type.Info)) return ArtifactAnnotation.Type.Info;
      return ArtifactAnnotation.Type.None;
   }

   public Image getImage() {
      if (accessManager.hasLock(this)) {
         return descriptor.getLockedImage(accessManager.hasLockAccess(this));
      }

      try {
         if (getArtifactTypeName().equals("Version")) {
            boolean next = getSoleBooleanAttributeValue("ats.Next Version");
            boolean released = getSoleBooleanAttributeValue("ats.Released");
            return descriptor.getImage(next, released);
         }
      } catch (IllegalStateException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return descriptor.getAnnotationImage(getMainAnnotationType());
   }

   public boolean isVersionControlled() {
      return true;
      // return controlLevel.isVersionControlled();
   }

   public boolean hasArtifacts(IRelationEnumeration side) {
      try {
         return getLinkManager().hasArtifacts(side);
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return false;

   }

   public Set<Artifact> getArtifacts(IRelationEnumeration side) throws SQLException {
      return getLinkManager().getArtifacts(side);
   }

   public Artifact getFirstArtifact(IRelationEnumeration side) throws SQLException {
      Collection<Artifact> arts = this.getArtifacts(side);
      if (arts.size() > 0) return arts.iterator().next();
      return null;
   }

   @SuppressWarnings("unchecked")
   public <A extends Artifact> Set<A> getArtifactsViaSearch(IRelationEnumeration side, Class<A> clazz) throws SQLException {
      if (isLinkManagerLoaded()) return getArtifacts(side, clazz);
      LinkedList<ISearchPrimitive> thisArtCriteria = new LinkedList<ISearchPrimitive>();
      thisArtCriteria.add(new ArtifactHridSearch(getHumanReadableId()));
      FromArtifactsSearch thisArtSearch = new FromArtifactsSearch(thisArtCriteria, true);

      LinkedList<ISearchPrimitive> relationCriteria = new LinkedList<ISearchPrimitive>();
      relationCriteria.add(new InRelationSearch(thisArtSearch, side));

      Set<A> arts = new HashSet<A>();
      try {
         for (Artifact a : ArtifactPersistenceManager.getInstance().getArtifacts(relationCriteria, true,
               this.getBranch())) {
            arts.add((A) a);
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return arts;
   }

   @SuppressWarnings("unchecked")
   public <A extends Artifact> A getFirstArtifactViaSearch(RelationSide side, Class<A> clazz) {
      try {
         if (isLinkManagerLoaded()) return (A) getFirstArtifact(side);
         Set<A> arts = getArtifactsViaSearch(side, clazz);
         if (arts.size() > 0) return (A) arts.iterator().next();
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return null;
   }

   /**
    * @param <A>
    * @param side
    * @param clazz
    * @throws SQLException
    */
   public <A extends Artifact> Set<A> getArtifacts(IRelationEnumeration side, Class<A> clazz) throws SQLException {
      RelationLinkGroup group = getLinkManager().getGroup(side);

      if (group == null) return new HashSet<A>();

      return group.getArtifacts(clazz);
   }

   /**
    * Called upon completion of the initialization of an artifact when it is initially created. This allows sub-class
    * artifacts to set default attributes or do default processing.
    */
   public void onBirth() throws SQLException {
      checkDeleted();
   };

   /**
    * Called upon completion of the initialization of an artifact when loaded from the persistence layer, and when
    * initially created. When called upon initial creation, it is called after <code>onBirth()</code>. This allows
    * sub-class artifacts to set default attributes or do default processing.
    */
   public void onInitializationComplete() {
      checkDeleted();
   };

   /**
    * @return Returns the artId.
    */
   public int getArtId() {

      if (memo == null) throw new IllegalStateException("PersistenceMemo has not been set on this artifact");

      return memo.getArtId();
   }

   /**
    * @return Returns the artTypeId.
    */
   public int getArtTypeId() {
      return descriptor.getArtTypeId();
   }

   /**
    * @return Returns the branch.
    */
   public Branch getBranch() {
      return branch;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.core.Unique#getGUID()
    */
   public String getGuid() {
      return guid;
   }

   public String getArtifactTypeName() throws SQLException {
      checkDeleted();
      if (artifactTypeName == null) {
         int id = getArtTypeId();
         artifactTypeName = artifactManager.getArtifactTypeName(id);
      }
      return artifactTypeName;
   }

   public String getArtifactTypeNameSuppressException() {
      try {
         return getArtifactTypeName();
      } catch (SQLException ex) {
         return ex.getLocalizedMessage();
      }
   }

   public boolean isOfType(String artifactType) throws SQLException {
      if (artifactType.equals("Abstract Software Requirement") && (getArtifactTypeName().equals(
            Requirements.SOFTWARE_REQUIREMENT) || getArtifactTypeName().equals(
            Requirements.INDIRECT_SOFTWARE_REQUIREMENT))) {
         return true;
      }
      return getArtifactTypeName().equals(artifactType);
   }

   public String toString() {
      checkDeleted();
      if (attributesNotLoaded()) return "<name not loaded yet>";
      return getDescriptiveName();
   }

   /**
    * also initializes the attributes if necessary
    * 
    * @return The user defined attribute type's for this artifact.
    * @throws SQLException
    * @throws SQLException
    */
   public Collection<DynamicAttributeManager> getAttributeManagers() throws SQLException {
      checkDeleted();
      acquireAttributes(false);
      return attributeManagers;
   }

   public Artifact getParent() throws SQLException {
      return getLinkManager().getSoleArtifact(DEFAULT_HIERARCHICAL__PARENT);
   }

   public boolean isOrphan() throws SQLException {
      Artifact root = artifactManager.getDefaultHierarchyRootArtifact(getBranch());
      for (Artifact parent = getParent(); parent != null; parent = parent.getParent()) {
         if (parent.equals(root)) {
            return false;
         }
      }
      return true;
   }

   public Artifact getChild(String descriptiveName) throws SQLException {
      for (Artifact artifact : getChildren()) {
         if (artifact.getDescriptiveName().equals(descriptiveName)) {
            return artifact;
         }
      }
      throw new IllegalArgumentException("No child with the name \"" + descriptiveName + "\" exists");
   }

   /**
    * @return set of the direct children of this artifact
    * @throws SQLException
    */
   public Set<Artifact> getChildren() throws SQLException {
      checkDeleted();
      checkLinkManager(false);

      return getArtifacts(DEFAULT_HIERARCHICAL__CHILD);
   }

   /**
    * @return a list of artifacts ordered by a depth first traversal of this artifact's descendants
    * @throws SQLException
    */
   public List<Artifact> getDescendants() throws SQLException {
      List<Artifact> descendants = new LinkedList<Artifact>();
      getDescendants(descendants);
      return descendants;
   }

   private void getDescendants(Collection<Artifact> descendants) throws SQLException {
      checkDeleted();
      for (Artifact child : getChildren()) {
         descendants.add(child);
         child.getDescendants(descendants);
      }
   }

   /**
    * @param artifact
    * @throws SQLException
    */
   public void addChild(Artifact artifact) throws SQLException {
      checkDeleted();

      relate(DEFAULT_HIERARCHICAL__CHILD, artifact);
   }

   /**
    * creates a new child using descriptor, relates it to its parent, and persists the child
    * 
    * @param descriptor
    * @param name TODO
    * @throws SQLException
    */
   public void addNewChild(ArtifactSubtypeDescriptor descriptor, String name) throws SQLException {
      Artifact child = descriptor.makeNewArtifact(branch);
      child.setDescriptiveName(name);
      addChild(child);
      child.persistAttributes();
      child.getLinkManager().persistLinks();
   }

   /**
    * A lightweight method for acquiring if this artifact has children. This works by not acquiring the full relation
    * from the database to just count the children. Instead, if the relation is not available, an alternative database
    * call is made.
    * 
    * @return Whether this <code>Artifact</code> has children.
    * @throws SQLException
    */
   public boolean hasChildren() throws SQLException {
      checkDeleted();

      if (isLinkManagerLoaded()) return getChildren().size() > 0;

      return artifactManager.getArtifactDhChildCount(memo.getArtId(), getBranch()) > 0;
   }

   public void addChildren(List<? extends Artifact> artifacts) throws SQLException {
      checkDeleted();
      for (Artifact artifact : artifacts) {
         addChild(artifact);
      }
   }

   public void setAttribute(SkynetAttributeChange attrChange) throws SQLException {
      DynamicAttributeManager userAttr = getAttributeManager(attrChange.getName());
      userAttr.getAttribute(attrChange).setStringData(attrChange.getValue());
   }

   /**
    * Get the attribute Manager for the attribute(s) of the name attributeName for this artifact.</br></br>
    * 
    * @param attributeName
    * @throws IllegalStateException if the provided name does not match any of the available attributes for this
    *            artifact.
    * @return attribute
    * @throws SQLException
    * @throws SQLException
    */
   public DynamicAttributeManager getAttributeManager(DynamicAttributeDescriptor attributeType) throws SQLException {
      checkDeleted();

      for (DynamicAttributeManager attributeManager : getAttributeManagers()) {
         if (attributeManager.getAttributeType().equals(attributeType)) {
            return attributeManager;
         }
      }

      throw new IllegalStateException(String.format(
            "The attribute \'%s\' is not valid for artifact type \'%s\' named \'%s\' guid \'%s\'",
            attributeType.getName(), getArtifactTypeName(), getDescriptiveName(), getGuid()));
   }

   /**
    * Get the attribute Manager for the attribute(s) of the name attributeName for this artifact.</br></br>
    * 
    * @param attributeName
    * @throws IllegalStateException if the provided name does not match any of the available attributes for this
    *            artifact.
    * @return attribute
    * @throws SQLException
    * @throws SQLException
    */
   public DynamicAttributeManager getAttributeManager(String attributeName) throws SQLException {
      return getAttributeManager(configurationManager.getDynamicAttributeType(attributeName));
   }

   /**
    * @param attributeName
    * @return true if attributeName is valid for the artifact type of this artifact
    * @throws SQLException
    */
   public boolean isAttributeTypeValid(String attributeName) throws SQLException {
      Collection<DynamicAttributeDescriptor> attributeTypes =
            configurationManager.getAttributeTypesFromArtifactType(getArtifactTypeName(), branch);
      for (DynamicAttributeDescriptor attributeType : attributeTypes) {
         if (attributeType.getName().equals(attributeName)) {
            return true;
         }
      }
      return false;
   }

   public <T> Collection<Attribute<T>> getAttributes(String attributeTypeName) throws SQLException {
      return getAttributeManager(attributeTypeName).getAttributes();
   }

   public Collection<DynamicAttributeDescriptor> getAttributeTypes() throws SQLException {
      return configurationManager.getAttributeTypesFromArtifactType(getArtifactTypeName(), branch);
   }

   public <T> Collection<Attribute<T>> getAttributes(DynamicAttributeDescriptor attributeType) throws SQLException {
      return getAttributeManager(attributeType).getAttributes();
   }

   private <T> Attribute<T> getSoleAttribute(String attributeTypeName) throws IllegalStateException, SQLException {
      return getSoleAttribute(getAttributeManager(attributeTypeName));
   }

   /**
    * This method is for use on min 0, max 1 attributes. If the attribute exists, it's string value is returned.
    * Otherwise empty string is returned.
    * 
    * @param attributeName
    * @return Attribute string.
    * @throws IllegalStateException
    */
   public String getSoleStringAttributeValue(String attributeTypeName) throws IllegalStateException {
      String value = getSoleXAttributeValueHideException(attributeTypeName);
      return value == null ? "" : value;
   }

   private <T> Attribute<T> getSoleAttribute(DynamicAttributeDescriptor attributeType) throws IllegalStateException, SQLException {
      return getSoleAttribute(getAttributeManager(attributeType));
   }

   private <T> Attribute<T> getSoleAttribute(DynamicAttributeManager attributeManager) throws IllegalStateException, SQLException {
      Collection<Attribute<T>> attributes = attributeManager.getAttributes();
      if (attributes.size() > 1) {
         throw new IllegalStateException(String.format(
               "The attribute \'%s\' can only have max 1 for sole attribute operations; guid \'%s\'",
               attributeManager.getAttributeType().getName(), getGuid()));
      } else if (attributes.size() == 0) {
         return null;
      }
      return (attributes.iterator().next());
   }

   private <T> Attribute<T> getSoleAttributeForSet(String attributeTypeName) throws IllegalStateException, SQLException {
      return getSoleAttributeForSet(getAttributeManager(attributeTypeName));
   }

   private <T> Attribute<T> getSoleAttributeForSet(DynamicAttributeDescriptor attributeType) throws IllegalStateException, SQLException {
      return getSoleAttributeForSet(getAttributeManager(attributeType));
   }

   private <T> Attribute<T> getSoleAttributeForSet(DynamicAttributeManager attributeManager) throws IllegalStateException, SQLException {
      Attribute<T> attribute = getSoleAttribute(attributeManager);

      if (attribute == null) {
         return attributeManager.getNewAttribute();
      }
      return attribute;
   }

   /**
    * Get sole integer attribute value if it exists, else return 0
    * 
    * @param attributeName
    * @return
    * @throws IllegalStateException
    */
   public int getSoleIntegerAttributeValue(String attributeTypeName) throws IllegalStateException {
      Integer value = getSoleXAttributeValueHideException(attributeTypeName);
      if (value == null) return 0;
      return value;
   }

   /**
    * @param attributeName
    * @return true if attribute exists AND set to "yes"; else return false
    * @throws IllegalStateException
    * @throws SQLException
    */
   public boolean getSoleBooleanAttributeValue(String attributeTypeName) throws IllegalStateException, SQLException {
      Boolean result = getSoleXAttributeValue(attributeTypeName);
      if (result == null) return false;
      return result;
   }

   public <T> T getSoleXAttributeValue(String attributeTypeName) throws IllegalStateException, SQLException {
      Attribute<T> attribute = getSoleAttribute(attributeTypeName);
      if (attribute == null) {
         return null;
      }
      return attribute.getValue();
   }

   public <T> T getSoleXAttributeValue(String attributeTypeName, Class<T> clazz) throws IllegalStateException, SQLException {
      Attribute<T> attribute = getSoleAttribute(attributeTypeName);
      if (attribute == null) {
         return null;
      }
      return (T) attribute.getValue();
   }

   public <T> T getSoleXAttributeValueHideException(String attributeTypeName) throws IllegalStateException {
      try {
         return getSoleXAttributeValue(attributeTypeName);

      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         return null;
      }
   }

   public <T> void setSoleXAttributeValue(String attributeTypeName, T value) throws IllegalStateException, SQLException {
      Attribute<T> attribute = getSoleAttributeForSet(attributeTypeName);
      attribute.setValue(value);
   }

   public void setSoleAttributeFromStream(String attributeTypeName, InputStream stream) throws IllegalStateException, SQLException, IOException {
      Attribute<Object> attribute = getSoleAttributeForSet(attributeTypeName);
      attribute.setValueFromInputStream(stream);
   }

   /**
    * This method is for use on min 0, max 1 attributes. If the attribute exists, it's string is replaced with value.
    * Else, an attribute is added and value set.
    * 
    * @param attributeName
    * @throws IllegalStateException
    * @throws SQLException
    */
   public void setSoleStringAttributeValue(String attributeTypeName, String value) throws IllegalStateException, SQLException {
      setSoleXAttributeValue(attributeTypeName, value);
   }

   public void clearSoleAttributeValue(String attributeTypeName) throws IllegalStateException, SQLException {
      Attribute<String> attribute = getSoleAttribute(attributeTypeName);

      if (attribute != null) {
         attribute.delete();
      }
   }

   public void setSoleBooleanAttributeValue(String attributeTypeName, Boolean value) throws IllegalStateException, SQLException {
      setSoleXAttributeValue(attributeTypeName, value);
   }

   public void setSoleDateAttributeValue(String attributeTypeName, Date value) throws IllegalStateException, SQLException {
      setSoleXAttributeValue(attributeTypeName, value);
   }

   /**
    * @param attributeName
    * @return comma delimited represenatation of all the attributes of the type attributeName
    * @throws SQLException
    */
   public String getAttributesToString(String attributeName) throws SQLException {
      checkDeleted();
      StringBuffer sb = new StringBuffer();
      DynamicAttributeManager dam = getAttributeManager(attributeName);
      for (Attribute attr : dam.getAttributes())
         sb.append(attr.getStringData() + ", ");
      return sb.toString().replaceFirst(", $", "");
   }

   /**
    * Uses the Dynamic Attribute Manager to set a group of attribute data strings into a set of attributes. Checks to
    * see if data value exists before adding and also removes those attribute values that are not in the input dataStrs.
    * 
    * @param attributeName
    * @param dataStrs
    * @throws SQLException
    */
   public void setDamAttributes(String attributeName, Collection<String> dataStrs) throws SQLException {
      ArrayList<String> storedNames = new ArrayList<String>();
      DynamicAttributeManager dam = getAttributeManager(attributeName);
      int minOccur = dam.getAttributeType().getMinOccurrences();
      int maxOccur = dam.getAttributeType().getMaxOccurrences();
      for (Attribute attr : getAttributeManager(attributeName).getAttributes()) {
         storedNames.add(attr.getStringData());
      }

      if (dataStrs.size() > maxOccur) throw new IllegalStateException(
            "Attempting to set " + dataStrs.size() + " when max =" + maxOccur);
      if (dataStrs.size() < minOccur) throw new IllegalStateException(
            "Attempting to set " + dataStrs.size() + " when min =" + minOccur);
      // If size to replace is same as size filled, need to reset existing attributes cause can't
      // add and
      // then remove
      if (dataStrs.size() == maxOccur && !storedNames.equals(dataStrs)) {
         String[] dataStrsArr = dataStrs.toArray(new String[dataStrs.size()]);
         int x = 0;
         for (Attribute attr : getAttributeManager(attributeName).getAttributes())
            attr.setStringData(dataStrsArr[x++]);
         return;
      }

      // Add items that are newly selected
      for (String sel : dataStrs) {
         if (!storedNames.contains(sel)) {
            getAttributeManager(attributeName).getNewAttribute().setStringData(sel);
         }
      }

      // Remove items that aren't selected anymore
      for (String stored : storedNames) {
         if (!dataStrs.contains(stored)) {
            for (Attribute attr : getAttributeManager(attributeName).getAttributes())
               if (attr.getStringData().equals(stored)) attr.delete();
         }
      }
   }

   public <T> void addAttribute(String attributeTypeName, T value) throws SQLException {
      DynamicAttributeManager attributeManager = getAttributeManager(attributeTypeName);
      Attribute attribute = attributeManager.getNewAttribute();
      attribute.setValue(value);
   }

   /**
    * @param attributeName
    * @return string collection representation of all the attributes of the type attributeName
    * @throws SQLException
    */
   public Set<String> getAttributesToStringCollection(String attributeName) throws SQLException {
      checkDeleted();
      Set<String> items = new HashSet<String>();
      if (!isAttributeTypeValid(attributeName)) return items;
      DynamicAttributeManager dam = getAttributeManager(attributeName);
      for (Attribute attr : dam.getAttributes())
         items.add(attr.getStringData());
      return items;
   }

   public String getDescriptiveName() {
      try {
         if (!isAttributeTypeValid("Name")) {
            throw new IllegalStateException(String.format(
                  "Artifact Type [%s] guid [%s] does not have the attribute type 'Name' which is required.",
                  getArtifactTypeName(), getGuid()));
         }
         Attribute<String> attribute = getSoleAttribute("Name");
         if (attribute == null) {
            return UNNAMED;
         } else {
            return attribute.getValue();
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
         return ex.getLocalizedMessage();
      }
   }

   public void setDescriptiveName(String name) {
      try {
         setSoleXAttributeValue("Name", name);
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   private void acquireAttributes(boolean force) throws SQLException {
      if (force || attributeManagers == null) {
         artifactManager.setAttributesOnArtifact(this);
      }
   }

   public int getFactoryId() {
      checkDeleted();
      return parentFactory.getFactoryId();
   }

   public IArtifactFactory getFactory() {
      return parentFactory;
   }

   /**
    * This is used to mark that the artifact has been persisted. This should only be called by the
    * ArtifactPersistenceManager.
    */
   public void setNotDirty() {
      checkDeleted();
      dirty = false;

      if (attributeManagers != null) {
         for (DynamicAttributeManager userAttr : attributeManagers) {
            for (Attribute attr : userAttr.getAttributes()) {
               attr.setNotDirty();
            }
            userAttr.setDirty(false);
         }
      }
   }

   /**
    * Not supported in the public API for internal use only.
    * 
    * @param inTransaction
    */
   public void setInTransaction(boolean inTransaction) {
      this.inTransaction = inTransaction;
   }

   protected boolean isInTransaction() {
      return inTransaction;
   }

   /**
    * @return Returns the dirty.
    * @throws SQLException
    */
   public boolean isDirty() throws SQLException {
      return isDirty(false);
   }

   /**
    * @return Returns the dirty.
    * @throws SQLException
    */
   public boolean isDirty(boolean includeLinks) throws SQLException {
      checkDeleted();

      boolean dirtyVal = dirty || anAttributeIsDirty();
      if (includeLinks) {
         checkLinkManager(false);
         dirtyVal |= getLinkManager().isDirty();
      }
      return dirtyVal;
   }

   public boolean isReadOnly() {
      return (memo != null && !memo.getTransactionId().isEditable()) || !accessManager.checkObjectPermission(this,
            PermissionEnum.WRITE);
   }

   private boolean anAttributeIsDirty() {

      // An attribute can only be dirty if the attributes are loaded
      if (attributeManagers != null) {
         for (DynamicAttributeManager userAttr : attributeManagers) {

            if (userAttr.isDirty()) return true;
         }
      }

      return false;
   }

   protected void setAttributeManagers(Collection<DynamicAttributeManager> attributes) {
      checkDeleted();
      this.attributeManagers = attributes;
   }

   /**
    * Reverts this artifact back to the last state saved. This will have no effect if the artifact has not ever been
    * saved.
    * 
    * @throws SQLException
    * @throws IllegalStateException if the artifact is deleted
    */
   public void revert() throws SQLException {
      checkDeleted();

      if (!isInDb()) return;

      acquireAttributes(true);
      checkLinkManager(true);
      dirty = false;
      SkynetEventManager.getInstance().kick(new CacheArtifactModifiedEvent(this, ModType.Reverted, this));
   }

   public void persistAttributes() throws SQLException {
      persist(false, true);
   }

   public void persistAttributesAndLinks() throws SQLException {
      persistAttributes();
      getLinkManager().persistLinks();
   }

   public void persistAttributesAndLinks(Set<IRelationEnumeration> linkTypes) throws SQLException {
      persistAttributes();
      getLinkManager().persistLinks();
   }

   public void persist(boolean recurse) throws SQLException {
      persist(recurse, true);
   }

   /**
    * make this method private
    * 
    * @param recurse
    * @param persistAttributes
    * @throws SQLException
    */
   public void persist(boolean recurse, boolean persistAttributes) throws SQLException {
      checkDeleted();
      if (artifactManager == null) {
         throw new IllegalStateException("The object \"" + this + "\" does not have an associated persistence manager.");
      }
      artifactManager.makePersistent(this, recurse, persistAttributes);
   }

   /**
    * Returns all of the descendants through the primary decomposition tree that have a particular human readable id.
    * This will not return the called upon node if the name matches since it can not be a descendant of itself.
    * 
    * @param humanReadableId The human readable id text to match against.
    * @param caseSensitive Whether to use case sensitive matching.
    * @return <code>Collection</code> of <code>Artifact</code>'s that match.
    */
   public Collection<Artifact> getDescendants(String humanReadableId, boolean caseSensitive) {
      checkDeleted();
      Collection<Artifact> descendants = new LinkedList<Artifact>();

      try {
         for (Artifact child : getChildren()) {
            if ((caseSensitive && child.getDescriptiveName().equals(humanReadableId)) || (!caseSensitive && child.getDescriptiveName().equalsIgnoreCase(
                  humanReadableId))) {
               descendants.add(child);
            }
            descendants.addAll(child.getDescendants(humanReadableId, caseSensitive));
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      return descendants;
   }

   /**
    * Returns all of the descendants through the primary decomposition tree that are already loaded.
    */
   public Collection<Artifact> getLoadedDescendants() {
      checkDeleted();
      Collection<Artifact> descendants = new LinkedList<Artifact>();

      try {
         if (isLinkManagerLoaded()) {
            for (Artifact child : getChildren()) {
               descendants.add(child);
               descendants.addAll(child.getLoadedDescendants());
            }
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      return descendants;
   }

   /**
    * Return relations that exist between artifacts
    * 
    * @throws SQLException
    */
   public ArrayList<IRelationLink> getRelations(Artifact artifact) throws SQLException {
      ArrayList<IRelationLink> links = new ArrayList<IRelationLink>();
      for (IRelationLink link : getLinkManager().getLinks()) {
         if (getLinkManager().getOtherSideAritfact(link).equals(artifact)) links.add(link);
      }
      return links;
   }

   public ArrayList<IRelationLink> getRelations(IRelationType relationType) throws SQLException {
      ArrayList<IRelationLink> links = new ArrayList<IRelationLink>();
      for (IRelationLink link : getLinkManager().getLinks()) {
         if (link.getRelationType().equals(relationType)) {
            links.add(link);
         }
      }
      return links;
   }

   /**
    * Return relations that exist between artifacts of type side
    * 
    * @throws SQLException
    */
   public ArrayList<IRelationLink> getRelations(IRelationEnumeration side, Artifact artifact) throws SQLException {
      ArrayList<IRelationLink> links = new ArrayList<IRelationLink>();
      for (IRelationLink link : getLinkManager().getLinks()) {
         if (getLinkManager().getOtherSideAritfact(link).equals(artifact)) if (side.isThisType(link)) links.add(link);
      }
      return links;
   }

   /**
    * Removes artifact from a specific branch
    * 
    * @throws SQLException
    */
   public void delete() throws Exception {
      checkDeleted();

      artifactManager.deleteArtifact(this);
   }

   /**
    * Remove artifact from the database
    * 
    * @throws SQLException
    */
   public void purge() throws SQLException {
      checkDeleted();

      artifactManager.purgeArtifact(this);
   }

   public boolean isDeleted() {
      return deleted;
   }

   protected void setDeleted() {
      checkDeleted();
      if (isInDb()) parentFactory.deCache(this);
      this.deleted = true;
   }

   protected void checkDeleted() {
      if (deleted && !deleteCheckOveride) throw new IllegalStateException("This artifact has been deleted");
   }

   public void setDirty() {
      dirty = true;
   }

   /**
    * @return Returns the linkManager.
    * @throws SQLException
    */
   public LinkManager getLinkManager() throws SQLException {
      checkLinkManager(false);
      return linkManager;
   }

   public LinkManager createOrGetEmptyLinkManager() {
      try {
         if (linkManager == null) {
            linkManager = new LinkManager(this);
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return linkManager;
   }

   /**
    * @param relationSide
    * @param artifact
    * @throws SQLException
    */
   public void relate(IRelationEnumeration relationSide, Artifact artifact) throws SQLException {
      relate(relationSide, artifact, null, false);
   }

   public void relate(IRelationEnumeration relationSide, Artifact artifact, boolean persist) throws SQLException {
      relate(relationSide, artifact, null, persist);
   }

   public void relate(IRelationEnumeration relationSide, Artifact artifact, String rationale, boolean persist) throws SQLException {
      getLinkManager().ensureRelationGroupExists(relationSide).addArtifact(artifact, rationale, persist);
   }

   public void relate(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts, boolean persist) throws SQLException {
      relate(relationSide, artifacts, null, persist);
   }

   public void relate(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts, String rationale, boolean persist) throws SQLException {
      for (Artifact art : artifacts)
         relate(relationSide, art, rationale, persist);
   }

   public void relate(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts) {
      try {
         relate(relationSide, artifacts, false);
      } catch (SQLException ex) {
         // This should never happen
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   public void unrelate(IRelationEnumeration relationSide, Artifact artifact) {
      try {
         unrelate(relationSide, artifact, false);
      } catch (SQLException ex) {
         // Should never happen because not persisting
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   public void unrelate(IRelationEnumeration relationSide, Artifact artifact, boolean persist) throws SQLException {
      getLinkManager().ensureRelationGroupExists(relationSide).removeArtifact(artifact);
      if (persist) linkManager.persistLinks();
   }

   public void relateReplace(IRelationEnumeration relationSide, Artifact artifact, boolean persist) throws SQLException {
      relateReplace(relationSide, Arrays.asList(new Artifact[] {artifact}), persist);
   }

   public void relateReplace(IRelationEnumeration relationSide, Collection<? extends Artifact> artifacts, boolean persist) throws SQLException {
      RelationLinkGroup group = getLinkManager().ensureRelationGroupExists(relationSide);
      group.removeAll();
      for (Artifact art : artifacts) {
         group.addArtifact(art);
      }
      if (persist) linkManager.persistLinks();
   }

   /**
    * Checks to make sure the linkManager has been created and populated with links. If the linkManager is null then it
    * will be created and populated.
    * 
    * @param revert
    */
   private void checkLinkManager(boolean revert) throws SQLException {

      if (revert && linkManager != null) {
         linkManager.releaseManager();
      }

      if (linkManager == null || revert) {
         linkManager = new LinkManager(this);
         linkManager.populateLinks();
      }
   }

   public final boolean attributesNotLoaded() {
      return attributeManagers == null;
   }

   public final boolean isLinkManagerLoaded() {
      return linkManager != null;
   }

   /**
    * @return Returns the humanReadableId.
    */
   public String getHumanReadableId() {
      return humanReadableId;
   }

   public void rollHumanReadableId() {
      humanReadableId = generateHumanReadableId();
   }

   private static final char[][] chars =
         new char[][] {
               {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
                     'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'},
               {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
                     'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'}};
   private static final int[] charsIndexLookup = new int[] {0, 1, 1, 1, 0};

   /**
    * 5 character human readable identifier where the first and last characters are in the range [A-Z0-9] except 'I' and
    * 'O' and the middle three characters have the same range as above with the additional restrictions of 'A', 'E', 'U'
    * thus the total number of unique values is: 34 * 31 * 31 *31 * 34 = 34,438,396
    */
   private static String generateHumanReadableId() {
      int seed = (int) (Math.random() * 34438396);
      char id[] = new char[charsIndexLookup.length];

      for (int i = 0; i < id.length; i++) {
         int radix = chars[charsIndexLookup[i]].length;
         id[i] = chars[charsIndexLookup[i]][seed % radix];
         seed = seed / radix;
      }
      return new String(id);
   }

   public ArtifactPersistenceMemo getPersistenceMemo() {
      return memo;
   }

   public void setPersistenceMemo(PersistenceMemo memo) {
      if (memo != null && memo instanceof ArtifactPersistenceMemo) {
         this.memo = (ArtifactPersistenceMemo) memo;
         parentFactory.cache(this);
         dirty = true;
      } else
         throw new IllegalArgumentException("Invalid memo type");
   }

   /**
    * @return Returns the descriptor.
    */
   public ArtifactSubtypeDescriptor getArtifactType() {
      return descriptor;
   }

   /**
    * @param descriptor The descriptor to set.
    */
   public void setDescriptor(ArtifactSubtypeDescriptor descriptor) {
      if (descriptor == null) {
         throw new IllegalArgumentException("a null descriptor was passed to setDescriptor");
      }
      this.descriptor = descriptor;
   }

   public String getVersionedName() {
      String name = getDescriptiveName();

      if (memo != null && !memo.getTransactionId().isEditable()) name += " [Rev:" + memo.getTransactionNumber() + "]";

      return name;
   }

   /**
    * Set the linkManager to null
    */
   public void clearLinkManager() {

      if (linkManager != null) {
         linkManager.setReleased();
      }
      linkManager = null;
   }

   /**
    * Return true if this artifact any of it's links specified or any of the artifacts on the other side of the links
    * are dirty
    * 
    * @param links
    * @param info TODO
    */
   public boolean isRelationsAndArtifactsDirty(Set<IRelationEnumeration> links, StringBuilder info) {
      try {
         if (isDirty()) {
            info.append(getArtifactTypeName() + " \"" + this + "\" => dirty\n");
            for (DynamicAttributeManager dam : getDirtyAttributes())
               if (dam.isDirty()) info.append("===> Dirty Attribute - " + dam.getAttributeType().getName() + "\n");
            return true;
         }
         // Loop through all relations
         for (IRelationEnumeration side : links) {
            for (Artifact art : getArtifacts(side)) {
               // Check artifact dirty
               if (art.isDirty()) {
                  info.append(art.getArtifactTypeName() + " \"" + art + "\" => dirty\n");
                  return true;
               }
               // Check the links to this artifact
               for (IRelationLink link : getRelations(side, art))
                  if (link.isDirty()) {
                     info.append("Link \"" + link + "\" => dirty\n");
                     return true;
                  }
            }
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
      return false;
   }

   /**
    * Save artifact, any of it's links specified, and any of the artifacts on the other side of the links that are dirty
    */
   public void saveArtifactsFromRelations(Set<IRelationEnumeration> links) throws SQLException {
      saveRevertArtifactsFromRelations(links, false);
   }

   /**
    * Revert artifact, any of it's links specified, and any of the artifacts on the other side of the links that are
    * dirty
    */
   public void revertArtifactsFromRelations(Set<IRelationEnumeration> links) throws SQLException {
      saveRevertArtifactsFromRelations(links, true);
   }

   @Deprecated
   //  use persistAttributesAndLinks() instead
   private void saveRevertArtifactsFromRelations(Set<IRelationEnumeration> links, boolean revert) throws SQLException {
      Set<Artifact> artifactToManipulate = new HashSet<Artifact>();
      artifactToManipulate.add(this);
      Set<IRelationLink> linksToManipulate = new HashSet<IRelationLink>();

      // Loop through all relations and collect all artifact to operate on
      for (IRelationEnumeration side : links) {
         for (Artifact artifact : getArtifacts(side)) {
            artifactToManipulate.add(artifact);
            // Check the links to this artifact
            for (IRelationLink link : getRelations(artifact)) {
               linksToManipulate.add(link);
            }
         }
      }
      // Loop through all relations and persist/revert as necessary
      for (IRelationLink link : linksToManipulate) {
         if (link.isDirty()) {
            if (revert) {
               link.delete();
            } else {
               link.persist();
            }
         }
      }
      // Loop through all artifacts and persist/revert as necessary
      for (Artifact artifact : artifactToManipulate) {
         if (revert) {
            artifact.revert();
         } else {
            artifact.persistAttributes();
         }
      }
      // Persist link manager to ensure deleted links get persisted
      //TODO: this defeats the whole purpose of a selective persist based on link type
      getLinkManager().persistLinks();
   }

   /**
    * Creates a new artifact and duplicates all of its attribute data.
    * 
    * @throws CloneNotSupportedException
    * @throws SQLException
    */
   public Artifact duplicate(Branch branch) throws CloneNotSupportedException, SQLException {
      Artifact newArtifact = descriptor.makeNewArtifact(branch);

      if (newArtifact.attributesNotLoaded()) {
         newArtifact.startAttributeInitialization();
         copyAttributes(newArtifact);
         newArtifact.finalizeAttributeInitialization();
      } else {
         copyAttributes(newArtifact);
      }

      return newArtifact;
   }

   private void copyAttributes(Artifact artifact) throws IllegalStateException, SQLException {
      for (DynamicAttributeManager attrManager : getAttributeManagers()) {
         for (Attribute attribute : attrManager.getAttributes()) {
            attribute.copyTo(artifact);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#clone()
    */
   @Override
   protected Object clone() throws CloneNotSupportedException {
      Artifact clonedArtifact = null;
      List<DynamicAttributeManager> attributeManagers = new LinkedList<DynamicAttributeManager>();

      try {
         // Need another way to create artifacts
         clonedArtifact =
               getFactory().getNewArtifact(getGuid(), getHumanReadableId(), getArtifactType().getName(), getBranch());
         clonedArtifact.setDescriptor(getArtifactType());
         clonedArtifact.setPersistenceMemo(new ArtifactPersistenceMemo(
               TransactionIdManager.getInstance().getEditableTransactionId(getBranch()), getArtId(),
               getPersistenceMemo().getGammaId()));
         getFactory().cache(clonedArtifact);

         for (DynamicAttributeManager attrManager : getAttributeManagers()) {
            attributeManagers.add((DynamicAttributeManager) attrManager.clone(clonedArtifact));
         }

         clonedArtifact.setAttributeManagers(attributeManagers);
         clonedArtifact.onInitializationComplete();
         clonedArtifact.setNotDirty();

      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }

      return clonedArtifact;
   }

   public Collection<DynamicAttributeManager> getDirtyAttributes() {
      ArrayList<DynamicAttributeManager> dirtyAttrs = new ArrayList<DynamicAttributeManager>();
      if (attributeManagers == null) return dirtyAttrs;
      for (DynamicAttributeManager userAttr : attributeManagers) {
         if (userAttr.isDirty()) dirtyAttrs.add(userAttr);
      }
      return dirtyAttrs;
   }

   /**
    * @return Returns dirty attributes.
    * @throws SQLException
    */
   public Collection<SkynetAttributeChange> getDirtyAttributeSkynetAttributeChanges() throws SQLException {
      List<SkynetAttributeChange> dirtyAttributes = new LinkedList<SkynetAttributeChange>();

      for (DynamicAttributeManager attributeManager : getAttributeManagers()) {
         for (Attribute attribute : attributeManager.getAttributes()) {
            if (attribute.isDirty()) {
               dirtyAttributes.add(new SkynetAttributeChange(attribute.getAttributeType().getName(),
                     attribute.getStringData(), attribute.getPersistenceMemo().getAttrId(),
                     attribute.getPersistenceMemo().getGammaId()));
            }
         }
      }
      return dirtyAttributes;
   }

   public void setAttributesNotDirty() throws SQLException {
      for (DynamicAttributeManager attributeManager : getAttributeManagers()) {
         for (Attribute attribute : attributeManager.getAttributes()) {
            attribute.setNotDirty();
         }
      }
   }

   /**
    * Changes the artifact type in the database.
    * 
    * @param descriptor
    * @throws SQLException
    */
   public void changeArtifactType(ArtifactSubtypeDescriptor descriptor) throws SQLException {
      artifactManager.changeArtifactSubStype(this, descriptor);
      setDescriptor(descriptor);
   }

   /**
    * @param attribute
    */
   public void purgeAttribute(Attribute attribute) throws SQLException {
      artifactManager.purgeAttribute(attribute);
   }

   private static final Pattern safeNamePattern = Pattern.compile("[^A-Za-z0-9 ]");
   private static final String[] NUMBER =
         new String[] {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};

   /**
    * Since artifact names are free text it is important to reformat the name to ensure it is suitable as an element
    * name
    * 
    * @return artifact name in a form that is valid as an XML element
    */
   public String getSafeName() {
      String elementName = safeNamePattern.matcher(getDescriptiveName()).replaceAll("_");

      // Ensure the name did not end up empty
      if (elementName.equals("")) {
         elementName = "nameless";
      }

      // Fix the first character if it is a number by replacing it with its name
      char firstChar = elementName.charAt(0);
      if (firstChar >= '0' && firstChar <= '9') {
         elementName = NUMBER[firstChar - '0'] + elementName.substring(1);
      }

      if (elementName.length() > 75) {
         elementName = elementName.substring(0, 75);
      }

      return elementName;
   }

   @SuppressWarnings("deprecation")
   private Set<IArtifactAnnotation> artifactAnnotationExtensions;

   private Set<IArtifactAnnotation> getAnnotationExtensions() {
      if (artifactAnnotationExtensions != null) return artifactAnnotationExtensions;
      artifactAnnotationExtensions = new HashSet<IArtifactAnnotation>();
      IExtensionPoint point =
            Platform.getExtensionRegistry().getExtensionPoint(
                  "org.eclipse.osee.framework.skynet.core.ArtifactAnnotation");
      if (point == null) {
         System.err.println("Can't access ArtifactAnnotation extension point");
         return artifactAnnotationExtensions;
      }
      IExtension[] extensions = point.getExtensions();
      for (IExtension extension : extensions) {
         IConfigurationElement[] elements = extension.getConfigurationElements();
         String classname = null;
         String bundleName = null;
         for (IConfigurationElement el : elements) {
            if (el.getName().equals("ArtifactAnnotation")) {
               classname = el.getAttribute("classname");
               bundleName = el.getContributor().getName();
               if (classname != null && bundleName != null) {
                  Bundle bundle = Platform.getBundle(bundleName);
                  try {
                     Class<?> taskClass = bundle.loadClass(classname);
                     Object obj = taskClass.newInstance();
                     artifactAnnotationExtensions.add((IArtifactAnnotation) obj);
                  } catch (Exception ex) {
                     ex.printStackTrace();
                  }
               }

            }
         }
      }
      return artifactAnnotationExtensions;
   }

   /**
    * @return the annotationMgr
    */
   public AttributeAnnotationManager getAnnotationMgr() {
      if (annotationMgr == null) {
         annotationMgr = new AttributeAnnotationManager(this);
      }
      return annotationMgr;
   }

   /**
    * Sets up this artifact for attribute initialization. This can only be used on artifacts that have never been
    * persisted and have not had their attributes already loaded. Calling this method in any other situation will result
    * in an IllegalStateException.
    * 
    * @throws IllegalStateException
    * @throws SQLException
    */
   public synchronized void startAttributeInitialization() throws IllegalStateException, SQLException {
      if (memo != null) throw new IllegalStateException("Can't perform attribute initialization on persisted artifacts");
      if (!attributesNotLoaded()) throw new IllegalStateException(
            "Can't perform attribute initialization on artifacts with loaded attributes");

      Collection<DynamicAttributeDescriptor> attributeTypeDescriptors =
            configurationManager.getAttributeTypesFromArtifactType(getArtifactType(), branch);
      Collection<DynamicAttributeManager> attributes =
            new ArrayList<DynamicAttributeManager>(attributeTypeDescriptors.size());

      DynamicAttributeManager attribute;
      for (DynamicAttributeDescriptor attributeType : attributeTypeDescriptors) {
         attribute = attributeType.createAttributeManager(this, true);

         attribute.setupForInitialization(true);
         attributes.add(attribute);
      }

      this.setAttributeManagers(attributes);

      this.initializingAttributes = true;
   }

   /**
    * Finalizes the attribute settings when in attribute initialization mode. This involves setting up default
    * attributes with default values for attributes initialized with less than the min number of attributes for the
    * attribute type, and detecting attributes that are initialized with more values than the max allows for the
    * attribute type. Exceeding the max boundary will result in an exception.
    * 
    * @see
    * @throws IllegalStateException
    */
   public synchronized void finalizeAttributeInitialization() throws IllegalStateException {
      if (!initializingAttributes) throw new IllegalStateException("Artifact not in attribute initialization mode");

      for (DynamicAttributeManager attribute : attributeManagers) {
         attribute.enforceMinMaxConstraints();
      }

      initializingAttributes = false;
   }

   public final boolean isInAttributeInitialization() {
      return initializingAttributes;
   }

   public int getDeletionTransactionId() throws SQLException {
      if (deletionTransactionId == 0) {
         if (getPersistenceMemo() == null || getPersistenceMemo().getTransactionId().isHead()) {
            deletionTransactionId = -1;
         } else {
            deletionTransactionId = artifactManager.getDeletionTransactionId(getArtId(), getBranch().getBranchId());
         }
      }
      return deletionTransactionId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   public Object getAdapter(Class adapter) {
      if (adapter == null) throw new IllegalArgumentException("adapter can not be null");

      if (adapter.isInstance(this)) {
         return this;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   public int compareTo(Artifact otherArtifact) {
      if (otherArtifact == null || otherArtifact.isDeleted()) {
         return -1;
      } else if (this.isDeleted()) {
         return 1;
      }

      int diff;
      if (otherArtifact.equals(this)) {
    	  diff = 0;
      }
      else {
      try {
         diff = getDescriptiveName().compareTo(otherArtifact.getDescriptiveName());
      } catch (Exception ex) {
         diff = 0;
      }
       }

      return diff;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return 31 * guid.hashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Artifact) {
         return guid.hashCode() == ((Artifact) obj).getGuid().hashCode();
      }
      return false;
   }
}