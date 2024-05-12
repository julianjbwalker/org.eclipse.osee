/*********************************************************************
 * Copyright (c) 2017 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.ats.ide.demo.populate;

import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_1;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_2;
import static org.eclipse.osee.framework.core.enums.DemoBranches.SAW_Bld_3;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsArtifactToken;
import org.eclipse.osee.ats.api.demo.DemoArtifactToken;
import org.eclipse.osee.ats.core.demo.DemoUtil;
import org.eclipse.osee.ats.ide.demo.internal.Activator;
import org.eclipse.osee.ats.ide.demo.internal.AtsApiService;
import org.eclipse.osee.ats.ide.util.AtsUtilClient;
import org.eclipse.osee.ats.ide.util.ServiceUtil;
import org.eclipse.osee.framework.core.access.IAccessControlService;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.data.ArtifactTypeToken;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.BranchToken;
import org.eclipse.osee.framework.core.data.RelationTypeSide;
import org.eclipse.osee.framework.core.enums.CoreArtifactTokens;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.CoreUserGroups;
import org.eclipse.osee.framework.core.enums.DemoBranches;
import org.eclipse.osee.framework.core.enums.DemoUsers;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.util.OseeInf;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.OseeProperties;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.importing.parsers.IArtifactExtractor;
import org.eclipse.osee.framework.skynet.core.importing.parsers.MarkdownOutlineExtractor;
import org.eclipse.osee.framework.skynet.core.importing.parsers.WordOutlineExtractor;
import org.eclipse.osee.framework.skynet.core.importing.parsers.WordOutlineExtractorDelegate;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.skynet.core.utility.OseeInfo;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportOperationFactory;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactImportOperationParameter;
import org.eclipse.osee.framework.ui.skynet.Import.ArtifactResolverFactory;

/**
 * @author Donald G. Dunne
 */
public class Pdd10SetupAndImportReqs implements IPopulateDemoDatabase {

   private static final String ApplicabilityBasicTags =
      "<w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[A=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test that a is included</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature[A=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>feature[c]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test case insensitive &amp; default value</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End feature</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[B=(Choice1| Choice2) | A=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test OR in values and features</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[B=Choice1 &amp; A=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test AND in features</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[A=Excluded]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test before else feature text</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature Else</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test after else feature text</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration [Config1]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration[Config1]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test before else</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration Else</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test after else</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration[Config1=Excluded]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test excluding config1</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration[Config1 | Config2]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test OR configurations</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"009511DC\"><w:pPr><w:spacing w:after=\"0\"></w:spacing><w:sectPr wsp:rsidR=\"009511DC\"><w:ftr w:type=\"odd\"><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00DF6E46\"><w:pPr><w:pStyle w:val=\"para8pt\"></w:pStyle><w:jc w:val=\"center\"></w:jc></w:pPr><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"begin\"></w:fldChar></w:r><w:r wsp:rsidR=\"00A35FD3\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:instrText> PAGE </w:instrText></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"separate\"></w:fldChar></w:r><w:r wsp:rsidR=\"009511DC\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle><w:noProof></w:noProof></w:rPr><w:t>1</w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"end\"></w:fldChar></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>UNSPECIFIED - PLEASE TAG WITH CORRECT DATA RIGHTS ATTRIBUTE!!!</w:t></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contract No.: W58RGZ-14-D-0045/T.O. 0016</w:t></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Name: The Boeing Company</w:t></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Address: 5000 E. McDowell Road; Mesa, AZ 85215-9797 </w:t></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00DF6E46\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00DF6E46\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>The Government's rights to use, modify, reproduce,</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t> release, perform, display, or disclose this software are restricted by paragraph (b)(3) of the Rights in Noncommercial Computer Software and Noncommercial Computer Software Documentation clause contained in the above identified contract.  Any reproduction</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t> of computer software or portions thereof marked with this legend must also reproduce the markings.  Any person, other than the Government, who has been provided access to such software must promptly notify the above named Contractor. </w:t></w:r></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00DF6E46\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00DF6E46\" wsp:rsidRDefault=\"00A35FD3\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Copyright (c) 2017 </w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>– The Boeing Company</w:t></w:r></w:p></w:ftr><w:pgSz w:h=\"15840\" w:w=\"12240\"></w:pgSz><w:pgMar w:bottom=\"1440\" w:footer=\"432\" w:gutter=\"0\" w:header=\"432\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"></w:pgMar><w:cols w:space=\"720\"></w:cols></w:sectPr></w:pPr></w:p>";
   private static final String ApplicabilityEmbeddedTagsCase =
      "<w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[A=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test text before embedded feature</w:t></w:r></w:p><w:p wsp:rsidP=\"00E9626A\" wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>feature[c]</w:t></w:r></w:p><w:p wsp:rsidP=\"00E9626A\" wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test embedded features</w:t></w:r></w:p><w:p wsp:rsidP=\"00E9626A\" wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End feature</w:t></w:r><w:r><w:t>[c]</w:t></w:r></w:p><w:p wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test text after embedded feature</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature</w:t></w:r></w:p><w:p wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration [Config1]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Configuration[Config1]</w:t></w:r></w:p><w:p wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test embedded configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"00E9626A\" wsp:rsidRDefault=\"00E9626A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Configuration</w:t></w:r><w:r wsp:rsidR=\"00E9626A\"><w:t>[config1]</w:t></w:r></w:p><w:p wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature[A=Included]</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test text before embedded feature</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>feature[c]</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test embedded features</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End feature[c]</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test text after embedded feature</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Feature Else</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>Test text </w:t></w:r><w:r><w:t>inside else statement</w:t></w:r><w:r wsp:rsidR=\"00DD4E5E\"><w:t> with embedded feature</w:t></w:r></w:p><w:p wsp:rsidP=\"00AC3EB1\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr><w:r><w:t>End Feature</w:t></w:r></w:p><w:p wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00AC3EB1\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00146F38\"><w:pPr><w:spacing w:after=\"0\"></w:spacing><w:sectPr wsp:rsidR=\"00146F38\"><w:ftr w:type=\"odd\"><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00146F38\"><w:pPr><w:pStyle w:val=\"para8pt\"></w:pStyle><w:jc w:val=\"center\"></w:jc></w:pPr><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"begin\"></w:fldChar></w:r><w:r wsp:rsidR=\"00E32D10\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:instrText> PAGE </w:instrText></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"separate\"></w:fldChar></w:r><w:r wsp:rsidR=\"00DD4E5E\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle><w:noProof></w:noProof></w:rPr><w:t>2</w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"end\"></w:fldChar></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>UNSPECIFIED - PLEASE TAG WITH CORRECT DATA RIGHTS ATTRIBUTE!!!</w:t></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contract No.: W58RGZ-14-D-0045/T.O. 0016</w:t></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Name: The Boeing Company</w:t></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Address: 5000 E. M</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>cDowell Road; Mesa, AZ 85215-9797 </w:t></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00146F38\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00146F38\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>The Government's rights to use, modify, reproduce, release, perform, display, or disclose this software are restricted by paragraph (b)(3) of the Rights in Noncommercial Computer Software and Noncommercial Computer Soft</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>ware Documentation clause contained in the above identified contract.  Any reproduction of computer software or portions thereof marked with this legend must also reproduce the markings.  Any person, other than the Government, who has been provided access </w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>to such software must promptly notify the above named Contractor. </w:t></w:r></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00146F38\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00146F38\" wsp:rsidRDefault=\"00E32D10\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Copyright (c) 2017 – The Boeing Company</w:t></w:r></w:p></w:ftr><w:pgSz w:h=\"15840\" w:w=\"12240\"></w:pgSz><w:pgMar w:bottom=\"1440\" w:footer=\"432\" w:gutter=\"0\" w:header=\"432\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"></w:pgMar><w:cols w:space=\"720\"></w:cols></w:sectPr></w:pPr></w:p>";
   private static final String ApplicabilityTable =
      "<w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"></w:p><w:tbl><w:tblPr><w:tblW w:type=\"auto\" w:w=\"0\"></w:tblW><w:tblBorders><w:top w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:top><w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:left><w:bottom w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:bottom><w:right w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:right><w:insideH w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:insideH><w:insideV w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:insideV></w:tblBorders><w:tblLook w:val=\"04A0\"></w:tblLook></w:tblPr><w:tblGrid><w:gridCol w:w=\"2461\"></w:gridCol><w:gridCol w:w=\"822\"></w:gridCol><w:gridCol w:w=\"822\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"2179\"></w:gridCol></w:tblGrid><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>Feature[a] a1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>A8 End Feature[a]</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>B8</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>Feature[B=Choice1]C1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>C8 </w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>Configuration[Config1] D1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>D8 End Configuration</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E8</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>E8</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>F8 End Feature[B=Choice1]</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00C620A4\"><w:r><w:t>G8</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:p wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"007B2CA7\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:tbl><w:tblPr><w:tblW w:type=\"auto\" w:w=\"0\"></w:tblW><w:tblBorders><w:top w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:top><w:left w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:left><w:bottom w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:bottom><w:right w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:right><w:insideH w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:insideH><w:insideV w:color=\"auto\" w:space=\"0\" w:sz=\"4\" w:val=\"single\" wx:bdrwidth=\"10\"></w:insideV></w:tblBorders><w:tblLook w:val=\"04A0\"></w:tblLook></w:tblPr><w:tblGrid><w:gridCol w:w=\"2461\"></w:gridCol><w:gridCol w:w=\"822\"></w:gridCol><w:gridCol w:w=\"822\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"823\"></w:gridCol><w:gridCol w:w=\"2179\"></w:gridCol></w:tblGrid><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>Feature[a] a1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>A8 End Feature[a]</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>B8</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>Feature[B=Choice1]C1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>C8 </w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"006F3C1E\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>D8 </w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>Feature Else </w:t></w:r><w:r><w:t>E8</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>E8</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>F8 End Feature[B=Choice1]</w:t></w:r></w:p></w:tc></w:tr><w:tr wsp:rsidR=\"00183C52\" wsp:rsidTr=\"00183C52\"><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G1</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G2</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G3</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G4</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G5</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G6</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G7</w:t></w:r></w:p></w:tc><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"1197\"></w:tcW><w:shd w:color=\"auto\" w:fill=\"auto\" w:val=\"clear\"></w:shd></w:tcPr><w:p wsp:rsidP=\"009D4255\" wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:r><w:t>G8</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:p wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"006F3C1E\"><w:pPr><w:spacing w:after=\"0\"></w:spacing><w:sectPr wsp:rsidR=\"006F3C1E\"><w:ftr w:type=\"odd\"><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"007B2CA7\"><w:pPr><w:pStyle w:val=\"para8pt\"></w:pStyle><w:jc w:val=\"center\"></w:jc></w:pPr><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"begin\"></w:fldChar></w:r><w:r wsp:rsidR=\"00183C52\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:instrText> PAGE </w:instrText></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"separate\"></w:fldChar></w:r><w:r wsp:rsidR=\"006F3C1E\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle><w:noProof></w:noProof></w:rPr><w:t>1</w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"end\"></w:fldChar></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>UNSPECIFIED - PLEASE TAG WITH CORRECT DATA RIGHTS ATTRIBUTE!!!</w:t></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contract No.: W58RGZ-14-D-0045/T.O. 0016</w:t></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Name: The Boeing Company</w:t></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Address: 5000 E. McDowell Road; Mesa, AZ 85215-9797 </w:t></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"007B2CA7\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"007B2CA7\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>The Government's rights to use, modify, reproduce, release, perform, display, or disclose this software are restricted by paragraph (b)(3) of the Rights in Noncommercial Computer Software an</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>d Noncommercial Computer Software Documentation clause contained in the above identified contract.  Any reproduction of computer software or portions thereof marked with this legend must also reproduce the markings.  Any person, other than the Government, </w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>who has been provided access to such software must promptly notify the above named Contractor. </w:t></w:r></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"007B2CA7\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"007B2CA7\" wsp:rsidRDefault=\"00183C52\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Copyright (c) 2017 – The Boeing Company</w:t></w:r></w:p></w:ftr><w:pgSz w:h=\"15840\" w:w=\"12240\"></w:pgSz><w:pgMar w:bottom=\"1440\" w:footer=\"432\" w:gutter=\"0\" w:header=\"432\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"></w:pgMar><w:cols w:space=\"720\"></w:cols></w:sectPr></w:pPr></w:p>";
   private static final String ApplicabilityLists =
      "<w:p wsp:rsidP=\"001A3BB8\" wsp:rsidR=\"00C620A4\" wsp:rsidRDefault=\"00983D8A\"></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"00AC3EB1\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Feature[a] Test 1 End Feature[A]</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Feature[B=Choice1]Test 2</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Test 3 Feature Else</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Test 4 End Feature</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><w:ilvl w:val=\"0\"></w:ilvl><w:ilfo w:val=\"0\"></w:ilfo></w:listPr><w:ind w:left=\"720\"></w:ind></w:pPr></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><w:ilvl w:val=\"0\"></w:ilvl><w:ilfo w:val=\"0\"></w:ilfo></w:listPr><w:ind w:left=\"720\"></w:ind></w:pPr></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Feature[C=Included] test embedded lists</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Feature[A=Excluded] test inside embedded End Feature</w:t></w:r></w:p><w:p wsp:rsidP=\"009B239B\" wsp:rsidR=\"009B239B\" wsp:rsidRDefault=\"009B239B\"><w:pPr><w:pStyle w:val=\"bulletlvl1\"></w:pStyle><w:listPr><wx:t wx:val=\"·\"></wx:t><wx:font wx:val=\"Symbol\"></wx:font></w:listPr></w:pPr><w:r><w:t>Test last bullet End Feature[C=Included]</w:t></w:r></w:p><w:p wsp:rsidR=\"009511DC\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"006F3C1E\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\"></w:spacing></w:pPr></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00664D5C\"><w:pPr><w:spacing w:after=\"0\"></w:spacing><w:sectPr wsp:rsidR=\"00664D5C\"><w:ftr w:type=\"odd\"><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00664D5C\"><w:pPr><w:pStyle w:val=\"para8pt\"></w:pStyle><w:jc w:val=\"center\"></w:jc></w:pPr><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"begin\"></w:fldChar></w:r><w:r wsp:rsidR=\"00983D8A\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:instrText> PAGE </w:instrText></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"separate\"></w:fldChar></w:r><w:r wsp:rsidR=\"009B239B\"><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle><w:noProof></w:noProof></w:rPr><w:t>1</w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"PageNumber\"></w:rStyle></w:rPr><w:fldChar w:fldCharType=\"end\"></w:fldChar></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>UNSPECIFIED - PLEASE TAG WITH CORRECT DATA RIGHTS ATTRIBUTE!!!</w:t></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contract No.: W58RGZ-14-D-0045/T.O. 0016</w:t></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Name: The Boeing Company</w:t></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Contractor Address: 5000 E. McDowell Road; Mesa, AZ 85215-9797 </w:t></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00664D5C\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00664D5C\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>The Government's rights to use, modify, reproduce, release, perform, display, or disclose this software are restricted by paragraph (b)(3) of the Rights i</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>n Noncommercial Computer Software and Noncommercial Computer Software Documentation clause contained in the above identified contract.  Any reproduction of computer software or portions thereof marked with this legend must also reproduce the markings.  Any</w:t></w:r><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t> person, other than the Government, who has been provided access to such software must promptly notify the above named Contractor. </w:t></w:r></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00664D5C\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr></w:p><w:p wsp:rsidR=\"00664D5C\" wsp:rsidRDefault=\"00983D8A\"><w:pPr><w:spacing w:after=\"0\" w:before=\"0\" w:line=\"240\" w:line-rule=\"auto\"></w:spacing><w:jc w:val=\"both\"></w:jc><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:cs=\"Arial\"></w:rFonts><w:sz w:val=\"16\"></w:sz></w:rPr><w:t>Copyright (c) 2017 – The Boeing Company</w:t></w:r></w:p></w:ftr><w:pgSz w:h=\"15840\" w:w=\"12240\"></w:pgSz><w:pgMar w:bottom=\"1440\" w:footer=\"432\" w:gutter=\"0\" w:header=\"432\" w:left=\"1440\" w:right=\"1440\" w:top=\"1440\"></w:pgMar><w:cols w:space=\"720\"></w:cols></w:sectPr></w:pPr></w:p>";

   @Override
   public void run() {
      AtsUtilClient.setEmailEnabled(false);
      if (AtsApiService.get().getStoreService().isProductionDb()) {
         throw new IllegalStateException("PopulateDemoActions should not be run on production DB");
      }
      validateArtifactCache();

      OseeLog.log(Activator.class, Level.INFO, "Populate Demo Database");

      AtsApiService.get().reloadServerAndClientCaches();

      OseeInfo.setValue(OseeProperties.OSEE_DB, "demo");
      AtsApiService.get().setConfigValue("DatabaseName", "DEMO");

      // Import all requirements on SAW_Bld_1 Branch
      demoDbImportReqsTx();

      // Create traceability between System, Subsystem and Software requirements
      SkynetTransaction demoDbTraceability =
         TransactionManager.createTransaction(SAW_Bld_1, "PopulateDemoDatabaseSetupAndImportReqs");
      demoDbTraceabilityTx(demoDbTraceability, SAW_Bld_1);
      demoDbTraceability.execute();

      // Note: SAW_Bld_1 is created during orcs dbinit in CreateDemoBranches
      BranchManager.setAssociatedArtifactId(SAW_Bld_1, AtsArtifactToken.AtsCmBranch);

      createNewBaselineBranch(SAW_Bld_1, SAW_Bld_2);
      createNewBaselineBranch(SAW_Bld_2, SAW_Bld_3);

      // Note: CIS_Bld_1 is created during orcs dbinit in CreateDemoBranches
      BranchManager.setAssociatedArtifactId(DemoBranches.CIS_Bld_1, AtsArtifactToken.AtsCmBranch);

      // Note: PL branches are created in CreateDemoBranches
      BranchManager.setAssociatedArtifactId(DemoBranches.SAW_PL, AtsArtifactToken.AtsCmBranch);
      setBaselineAccessControl(DemoBranches.SAW_PL);

      BranchManager.setAssociatedArtifactId(DemoBranches.SAW_PL_Hardening_Branch, AtsArtifactToken.AtsCmBranch);
      setBaselineAccessControl(DemoBranches.SAW_PL_Hardening_Branch);

      configureRequirementsForImplDetails();
   }

   private void setBaselineAccessControl(BranchToken branch) {
      IAccessControlService accessControlService = ServiceUtil.getOseeClient().getAccessControlService();
      accessControlService.removePermissions(branch);
      accessControlService.setPermission(DemoUsers.Kay_Jones, branch, PermissionEnum.FULLACCESS);
      accessControlService.setPermission(CoreUserGroups.Everyone, branch, PermissionEnum.READ);
   }

   private void configureRequirementsForImplDetails() {

      SkynetTransaction transaction =
         TransactionManager.createTransaction(SAW_Bld_1, "Configure Requirements for Impl Details");

      Artifact robotInterfaceHeader =
         ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotInterfaceHeading, SAW_Bld_1);
      Artifact robotUIHeading = ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotUserInterfaceHeading, SAW_Bld_1);
      Artifact robotAdminUI = ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotAdminUserInterface, SAW_Bld_1);
      Artifact robotAdminUIImpl =
         ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotAdminUserInterfaceImpl, SAW_Bld_1);
      Artifact robotUI = ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotUserInterface, SAW_Bld_1);
      Artifact robotUIImpl = ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotUserInterfaceImpl, SAW_Bld_1);
      Artifact robotCollabDetails = ArtifactTypeManager.addArtifact(DemoArtifactToken.RobotCollabDetails, SAW_Bld_1);
      Artifact eventDetailsHeader = ArtifactTypeManager.addArtifact(DemoArtifactToken.EventsDetailHeader, SAW_Bld_1);
      Artifact eventDetail = ArtifactTypeManager.addArtifact(DemoArtifactToken.EventsDetails, SAW_Bld_1);
      Artifact virtualFixDetailHeader =
         ArtifactTypeManager.addArtifact(DemoArtifactToken.VirtualFixDetailHeader, SAW_Bld_1);
      Artifact virtualFixDetail = ArtifactTypeManager.addArtifact(DemoArtifactToken.VirtualFixDetails, SAW_Bld_1);
      Artifact virtualFixDetailReq = ArtifactTypeManager.addArtifact(DemoArtifactToken.VirtualFixDetailReq, SAW_Bld_1);
      Artifact virtualFixDetailReqImpl =
         ArtifactTypeManager.addArtifact(DemoArtifactToken.VirtualFixDetailReqImplementation, SAW_Bld_1);

      Artifact robotInterface = ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.SoftwareRequirementMsWord,
         "Robot Interfaces", SAW_Bld_1);
      robotInterface.addChild(robotInterfaceHeader);
      transaction.addArtifact(robotInterfaceHeader);

      robotInterface.addChild(robotUIHeading);
      transaction.addArtifact(robotUIHeading);

      robotUIHeading.addChild(robotAdminUI);
      transaction.addArtifact(robotAdminUI);

      robotAdminUI.addChild(robotAdminUIImpl);
      transaction.addArtifact(robotAdminUIImpl);

      robotUIHeading.addChild(robotUI);
      transaction.addArtifact(robotUI);

      robotUI.addChild(robotUIImpl);
      transaction.addArtifact(robotUIImpl);

      Artifact robotCollab = ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.SoftwareRequirementMsWord,
         "Robot collaboration", SAW_Bld_1);
      robotCollab.addChild(robotCollabDetails);
      transaction.addArtifact(robotCollabDetails);

      Artifact events =
         ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.SoftwareRequirementMsWord, "Events", SAW_Bld_1);
      events.addChild(eventDetailsHeader);
      transaction.addArtifact(eventDetailsHeader);

      eventDetailsHeader.addChild(eventDetail);
      transaction.addArtifact(eventDetail);

      Artifact virtualFix = ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.SoftwareRequirementMsWord,
         "Virtual fixtures", SAW_Bld_1);
      virtualFix.addChild(virtualFixDetailHeader);
      transaction.addArtifact(virtualFixDetailHeader);

      virtualFixDetailHeader.addChild(virtualFixDetail);
      transaction.addArtifact(virtualFixDetail);

      virtualFixDetail.addChild(virtualFixDetailReq);
      transaction.addArtifact(virtualFixDetailReq);

      virtualFixDetailReq.addChild(virtualFixDetailReqImpl);
      transaction.addArtifact(virtualFixDetailReqImpl);

      transaction.execute();
   }

   private static void validateArtifactCache() {
      final Collection<Artifact> list = ArtifactCache.getDirtyArtifacts();
      if (!list.isEmpty()) {
         for (Artifact artifact : list) {
            OseeLog.log(Activator.class, Level.SEVERE, String.format("Artifact [%s] is dirty [%s]",
               artifact.toStringWithId(), Artifacts.getDirtyReport(artifact)));
         }
         throw new OseeStateException("[%d] Dirty Artifacts found after populate (see console for details)",
            list.size());
      }

   }

   private void createNewBaselineBranch(BranchToken parentBranch, BranchToken childBranch) {
      BranchManager.createBaselineBranch(parentBranch, childBranch);

      AtsApiService.get().getAccessControlService().setPermission(UserManager.getUser(DemoUsers.Joe_Smith), childBranch,
         PermissionEnum.FULLACCESS);
      BranchManager.setAssociatedArtifactId(childBranch, AtsArtifactToken.AtsCmBranch);
   }

   private void demoDbTraceabilityTx(SkynetTransaction transaction, BranchToken branch) {
      try {
         Collection<Artifact> systemArts = Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SystemRequirementMsWord, "Robot", branch));

         Collection<Artifact> component =
            Collections.castAll(DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.Component, "API", branch));
         component.addAll(Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.Component, "Hardware", branch)));
         component.addAll(
            Collections.castAll(DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.Component, "Sensor", branch)));

         Collection<Artifact> subSystemArts = Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SubsystemRequirementMsWord, "Robot", branch));
         subSystemArts.addAll(Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SubsystemRequirementMsWord, "Video", branch)));
         subSystemArts.addAll(Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SubsystemRequirementMsWord, "Interface", branch)));

         Collection<Artifact> softArts = Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SoftwareRequirementMsWord, "Robot", branch));
         softArts.addAll(Collections.castAll(
            DemoUtil.getArtTypeRequirements(debug, CoreArtifactTypes.SoftwareRequirementMsWord, "Interface", branch)));

         // Relate System to SubSystem to Software Requirements
         for (Artifact systemArt : systemArts) {
            relate(CoreRelationTypes.RequirementTrace_LowerLevelRequirement, systemArt, subSystemArts);
            systemArt.persist(transaction);

            for (Artifact subSystemArt : subSystemArts) {
               relate(CoreRelationTypes.RequirementTrace_LowerLevelRequirement, subSystemArt, softArts);
               subSystemArt.persist(transaction);
            }
         }

         // Relate System, SubSystem and Software Requirements to Componets
         for (Artifact art : systemArts) {
            relate(CoreRelationTypes.Allocation_Component, art, component);
            art.persist(transaction);
         }
         for (Artifact art : subSystemArts) {
            relate(CoreRelationTypes.Allocation_Component, art, component);
            art.persist(transaction);
         }
         for (Artifact art : softArts) {
            relate(CoreRelationTypes.Allocation_Component, art, component);
         }

         // Create Test Script Artifacts
         Set<Artifact> verificationTests = new HashSet<>();
         Artifact verificationHeader =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Folder, "Verification Tests", branch);
         if (verificationHeader == null) {
            throw new IllegalStateException("Could not find Verification Tests header");
         }
         for (String str : new String[] {"A", "B", "C"}) {
            Artifact newArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.TestCase,
               verificationHeader.getBranch(), "Verification Test " + str);
            verificationTests.add(newArt);
            verificationHeader.addRelation(CoreRelationTypes.DefaultHierarchical_Child, newArt);
            newArt.persist(transaction);
         }
         Artifact verificationTestsArray[] = verificationTests.toArray(new Artifact[verificationTests.size()]);

         // Create Validation Test Procedure Artifacts
         Set<Artifact> validationTests = new HashSet<>();
         Artifact validationHeader =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Folder, "Validation Tests", branch);
         if (validationHeader == null) {
            throw new IllegalStateException("Could not find Validation Tests header");
         }
         for (String str : new String[] {"1", "2", "3"}) {
            Artifact newArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.TestProcedure,
               validationHeader.getBranch(), "Validation Test " + str);
            validationTests.add(newArt);
            validationHeader.addRelation(CoreRelationTypes.DefaultHierarchical_Child, newArt);
            newArt.persist(transaction);
         }
         Artifact validationTestsArray[] = validationTests.toArray(new Artifact[validationTests.size()]);

         // Create Integration Test Procedure Artifacts
         Set<Artifact> integrationTests = new HashSet<>();
         Artifact integrationHeader =
            ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Folder, "Integration Tests", branch);
         if (integrationHeader == null) {
            throw new IllegalStateException("Could not find integration Tests header");
         }
         for (String str : new String[] {"X", "Y", "Z"}) {
            Artifact newArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.TestProcedure,
               integrationHeader.getBranch(), "integration Test " + str);
            integrationTests.add(newArt);
            integrationHeader.addRelation(CoreRelationTypes.DefaultHierarchical_Child, newArt);
            newArt.persist(transaction);
         }
         Artifact integrationTestsArray[] = integrationTests.toArray(new Artifact[integrationTests.size()]);

         // Relate Software Artifacts to Tests
         Artifact softReqsArray[] = softArts.toArray(new Artifact[softArts.size()]);
         softReqsArray[0].addRelation(CoreRelationTypes.Validation_Validator, verificationTestsArray[0]);
         softReqsArray[0].addRelation(CoreRelationTypes.Validation_Validator, verificationTestsArray[1]);
         softReqsArray[1].addRelation(CoreRelationTypes.Validation_Validator, verificationTestsArray[0]);
         softReqsArray[1].addRelation(CoreRelationTypes.Validation_Validator, validationTestsArray[1]);
         softReqsArray[2].addRelation(CoreRelationTypes.Validation_Validator, validationTestsArray[0]);
         softReqsArray[2].addRelation(CoreRelationTypes.Validation_Validator, integrationTestsArray[1]);
         softReqsArray[3].addRelation(CoreRelationTypes.Validation_Validator, integrationTestsArray[0]);
         softReqsArray[4].addRelation(CoreRelationTypes.Validation_Validator, integrationTestsArray[2]);
         softReqsArray[5].addRelation(CoreRelationTypes.Validation_Validator, validationTestsArray[2]);

         for (Artifact artifact : softArts) {
            artifact.persist(transaction);
         }

         createApplicabilityArtifacts(transaction, branch);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, Lib.exceptionToString(ex));
      }
   }

   private void relate(RelationTypeSide relationSide, Artifact artifact, Collection<Artifact> artifacts) {
      for (Artifact otherArtifact : artifacts) {
         artifact.addRelation(relationSide, otherArtifact);
      }
   }

   private void createApplicabilityArtifacts(SkynetTransaction transaction, BranchToken branch) {
      Artifact applicabilityFolder =
         ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.Folder, "Applicability Tests", branch);
      if (applicabilityFolder == null) {
         throw new IllegalStateException("Could not find Applicability Tests Folder");
      }

      //create a list of strings for all these tests
      String[] wordMlValue =
         new String[] {ApplicabilityBasicTags, ApplicabilityEmbeddedTagsCase, ApplicabilityTable, ApplicabilityLists};

      int i = 0;
      for (String str : new String[] {
         "ApplicabilityBasicTags", // OR/AND in features, Multi features/values, matching start/end tags, valid features in tags, default value works, exclude config, else tags
         "ApplicabilityEmbeddedTagsCase",
         "ApplicabilityTable",
         "ApplicabilityLists"}) {
         Artifact newArt = ArtifactTypeManager.addArtifact(CoreArtifactTypes.SoftwareRequirementMsWord, branch, str);
         newArt.addAttribute(CoreAttributeTypes.WordTemplateContent, wordMlValue[i]);
         applicabilityFolder.addRelation(CoreRelationTypes.DefaultHierarchical_Child, newArt);
         newArt.persist(transaction);
         i++;
      }
   }

   private void demoDbImportReqsTx() {
      try {
         //@formatter:off
         importWordXMLRequirements(SAW_Bld_1, CoreArtifactTypes.SoftwareRequirementMsWord, CoreArtifactTokens.SoftwareRequirementsFolder, OseeInf.getResourceAsFile("requirements/SAW-SoftwareRequirements.xml", getClass()));
         importWordXMLRequirements(SAW_Bld_1, CoreArtifactTypes.SystemRequirementMsWord, CoreArtifactTokens.SystemRequirementsFolder, OseeInf.getResourceAsFile("requirements/SAW-SystemRequirements.xml", getClass()));
         importWordXMLRequirements(SAW_Bld_1, CoreArtifactTypes.SubsystemRequirementMsWord, CoreArtifactTokens.SubSystemRequirementsFolder, OseeInf.getResourceAsFile("requirements/SAW-SubsystemRequirements.xml", getClass()));

         importMarkdownRequirements(SAW_Bld_1, CoreArtifactTypes.SystemRequirementMarkdown, CoreArtifactTokens.SystemRequirementsFolderMarkdown, OseeInf.getResourceAsFile("requirements/SAW-SystemRequirements.md", getClass()));
         importMarkdownRequirements(SAW_Bld_1, CoreArtifactTypes.SubsystemRequirementMarkdown, CoreArtifactTokens.SubSystemRequirementsFolderMarkdown, OseeInf.getResourceAsFile("requirements/SAW-SubsystemRequirements.md", getClass()));
         importMarkdownRequirements(SAW_Bld_1, CoreArtifactTypes.SoftwareRequirementMarkdown, CoreArtifactTokens.SoftwareRequirementsFolderMarkdown, OseeInf.getResourceAsFile("requirements/SAW-SoftwareRequirements.md", getClass()));
         importMarkdownRequirementImages(SAW_Bld_1, CoreArtifactTokens.SystemRequirementsFolderMarkdown);
         //@formatter:on
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, Lib.exceptionToString(ex));
      }
   }

   private void importMarkdownRequirementImages(BranchToken branch, ArtifactToken parentFolderTok) {

      SkynetTransaction transaction = TransactionManager.createTransaction(branch,
         "Populate Demo DB - Create Markdown Requirement Image Artifact(s)");

      Artifact parentFolderArt = ArtifactQuery.getArtifactFromTypeAndName(parentFolderTok.getArtifactType(),
         "System Requirements - Markdown", branch);

      // SAWTSR Image
      File sawtsrFile = OseeInf.getResourceAsFile("requirements/SAWTSR.png", getClass());
      Artifact sawtsrArt = ArtifactTypeManager.addArtifact(DemoArtifactToken.SAWTSR_Image_Markdown, branch);
      sawtsrArt.setSoleAttributeValue(CoreAttributeTypes.Extension, "png");
      // Set the native content attribute of general document artifact
      URI source = sawtsrFile.toURI();
      try {
         InputStream inputStream = source.toURL().openStream();
         sawtsrArt.setSoleAttributeValue(CoreAttributeTypes.NativeContent, inputStream);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, Lib.exceptionToString(ex));
      }

      // Robot Data Flow Image
      File robotDataFlowFile = OseeInf.getResourceAsFile("requirements/RobotDataFlow.png", getClass());
      Artifact robotDataFlowArt =
         ArtifactTypeManager.addArtifact(DemoArtifactToken.Robot_Data_Flow_Image_Markdown, branch);
      robotDataFlowArt.setSoleAttributeValue(CoreAttributeTypes.Extension, "png");
      // Set the native content attribute of general document artifact
      source = robotDataFlowFile.toURI();
      try {
         InputStream inputStream = source.toURL().openStream();
         robotDataFlowArt.setSoleAttributeValue(CoreAttributeTypes.NativeContent, inputStream);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, Lib.exceptionToString(ex));
      }

      // Add the general document artifacts to the parent folder
      parentFolderArt.addChild(sawtsrArt);
      parentFolderArt.addChild(robotDataFlowArt);
      transaction.addArtifact(sawtsrArt);
      transaction.addArtifact(robotDataFlowArt);

      transaction.execute();
   }

   private void importMarkdownRequirements(BranchId branch, ArtifactTypeToken requirementType, ArtifactToken folderTok,
      File file) {
      Artifact systemReqMd = ArtifactQuery.getArtifactFromId(folderTok, branch);

      IArtifactImportResolver artifactResolver =
         ArtifactResolverFactory.createAlwaysNewArtifacts(ArtifactTypeToken.SENTINEL);
      IArtifactExtractor extractor = new MarkdownOutlineExtractor(CoreArtifactTypes.HeadingMarkdown, requirementType);

      ArtifactImportOperationParameter importOptions = new ArtifactImportOperationParameter();
      importOptions.setSourceFile(file);
      importOptions.setDestinationArtifact(systemReqMd);
      importOptions.setExtractor(extractor);
      importOptions.setResolver(artifactResolver);

      IOperation operation = ArtifactImportOperationFactory.completeOperation(importOptions);
      Operations.executeWorkAndCheckStatus(operation);

      // Validate that something was imported
      if (systemReqMd.getChildren().isEmpty()) {
         throw new IllegalStateException("Artifacts were not imported");
      }
   }

   private void importWordXMLRequirements(BranchId branch, ArtifactTypeToken requirementType, ArtifactToken folderTok,
      File file) throws Exception {
      Artifact systemReq = ArtifactQuery.getArtifactFromId(folderTok, branch);

      IArtifactImportResolver artifactResolver = ArtifactResolverFactory.createAlwaysNewArtifacts(requirementType);
      IArtifactExtractor extractor = new WordOutlineExtractor();
      extractor.setDelegate(new WordOutlineExtractorDelegate());

      ArtifactImportOperationParameter importOptions = new ArtifactImportOperationParameter();
      importOptions.setSourceFile(file);
      importOptions.setDestinationArtifact(systemReq);
      importOptions.setExtractor(extractor);
      importOptions.setResolver(artifactResolver);

      IOperation operation = ArtifactImportOperationFactory.completeOperation(importOptions);
      Operations.executeWorkAndCheckStatus(operation);

      // Validate that something was imported
      if (systemReq.getChildren().isEmpty()) {
         throw new IllegalStateException("Artifacts were not imported");
      }
   }

}
