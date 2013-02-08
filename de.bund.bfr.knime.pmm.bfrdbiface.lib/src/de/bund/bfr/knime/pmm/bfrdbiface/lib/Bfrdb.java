/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * J�rgen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Th�ns (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.pmm.bfrdbiface.lib;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.hsh.bfr.db.DBKernel;
import org.hsh.bfr.db.MyLogger;

import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DbIo;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.LiteratureItem;
import de.bund.bfr.knime.pmm.common.MiscXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.ParametricModel;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmTimeSeries;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.PmmXmlElementConvertable;
import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model2Schema;

public class Bfrdb extends Hsqldbiface {
	
	public static final String ATT_AGENTDETAIL = "AgensDetail";
	public static final String ATT_AGENTID = "Agens";
	public static final String ATT_AGENTNAME = "Agensname";
	public static final String ATT_AW = "aw";
	public static final String ATT_COMBASEID = "ID_CB";
	public static final String ATT_COMMENT = "Kommentar";
	public static final String ATT_CONDITIONID = "Versuchsbedingung";
	public static final String ATT_CONDITIONS = "b_f_details_CB";
	public static final String ATT_CONDITION_MISCPARAM = "Versuchsbedingungen_Sonstiges";
	public static final String ATT_DEP = "Dependent";
	public static final String ATT_DESCRIPTION = "Beschreibung";
	public static final String ATT_ESTMODELID = "GeschaetztesModell";
	public static final String ATT_FIRSTAUTHOR = "Erstautor";
	public static final String ATT_FORMULA = "Formel";
	public static final String ATT_INDEP = "Independent";
	public static final String ATT_LEVEL = "Level";
	public static final String ATT_LITERATUREID = "Literatur";
	public static final String ATT_LITERATURETEXT = "ReferenzText";
	public static final String ATT_LOG10N = "Konzentration";
	public static final String ATT_LOG10NUNIT = "Konz_Einheit";
	public static final String ATT_MATRIXDETAIL = "MatrixDetail";
	public static final String ATT_MATRIXID = "Matrix";
	public static final String ATT_MATRIXNAME = "Matrixname";
	public static final String ATT_MAX = "max";
	public static final String ATT_MAXINDEP = "maxIndep";
	public static final String ATT_MAXVALUE = "maxValue";
	public static final String ATT_MIN = "min";
	public static final String ATT_MININDEP = "minIndep";
	public static final String ATT_MINVALUE = "minValue";
	public static final String ATT_MISC = "Sonstiges";
	public static final String ATT_MISCID = "SonstigesID";
	public static final String ATT_MODELID = "Modell";
	public static final String ATT_NAME = "Name";
	public static final String ATT_NAMESHORT = "Kurzbezeichnung";
	public static final String ATT_PARAMID = "Parameter";
	public static final String ATT_PARAMNAME = "Parametername";
	public static final String ATT_PARAMTYPE = "Parametertyp";
	public static final String ATT_PH = "pH";
	public static final String ATT_RMS = "RMS";
	public static final String ATT_RSQUARED = "Rsquared";
	public static final String ATT_RSS = "RSS";
	public static final String ATT_STANDARDERROR = "StandardError";
	public static final String ATT_TEMPERATURE = "Temperatur";
	public static final String ATT_TIME = "Zeit";
	public static final String ATT_TIMEUNIT = "ZeitEinheit";
	public static final String ATT_UNIT = "Einheit";
	public static final String ATT_VALUE = "Wert";
	public static final String ATT_VALUETYPE = "Wert_typ";
	public static final String ATT_VARMAPFROM = "VarPar";
	public static final String ATT_VARMAPTO = "VarParMap";
	public static final String ATT_YEAR = "Jahr";
	public static final String REL_AGENT = "Agenzien";
	public static final String REL_COMBASE = "ImportedCombaseData";
	public static final String REL_CONDITION = "Versuchsbedingungen";
	public static final String REL_DATA = "Messwerte";
	public static final String REL_DOUBLE = "DoubleKennzahlen";
	public static final String REL_ESTMODEL = "GeschaetzteModelle";
	public static final String REL_ESTPARAM = "GeschaetzteParameter";
	public static final String REL_MATRIX = "Matrices";
	public static final String REL_MISCPARAM = "SonstigeParameter";
	public static final String REL_MODEL = "Modellkatalog";
	public static final String REL_MODEL_LITERATURE = "Modell_Referenz";
	public static final String REL_PARAM = "ModellkatalogParameter";
	public static final String REL_UNIT = "Einheiten";
	public static final String REL_VARMAP = "VarParMaps";
	public static final String VIEW_CONDITION = "VersuchsbedingungenEinfach";
	public static final String VIEW_DATA = "MesswerteEinfach";
	public static final String VIEW_MISCPARAM = "SonstigesEinfach";

	private static final String queryTimeSeries7 = "SELECT\n"
			+"\n"
			+"    \""+VIEW_CONDITION+"\".\"ID\" AS \""+ATT_CONDITIONID+"\",\n"
			+"    \""+REL_COMBASE+"\".\"CombaseID\",\n"
			+"    \"MiscView\".\""+ATT_MISCID+"\",\n"
			+"    \"MiscView\".\""+ATT_MISC+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_TEMPERATURE+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_PH+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AW+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\",\n"
			+"    \""+REL_AGENT+"\".\""+ATT_AGENTNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\",\n"
			+"    \""+REL_MATRIX+"\".\""+ATT_MATRIXNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXDETAIL+"\",\n"
			+"    \"DataView\".\""+ATT_TIME+"\",\n"
			+"    \"DataView\".\""+ATT_LOG10N+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_COMMENT+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\"Referenz\" AS \""+ATT_LITERATUREID+"\",\n"
			+"    CONCAT( \""+ATT_LITERATUREID+"\".\""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_LITERATUREID+"\".\""+ATT_YEAR+"\" )AS \""+ATT_LITERATURETEXT+"\"\n"
			+"\n"
			+"FROM \""+VIEW_CONDITION+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_COMBASE+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\""+REL_COMBASE+"\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_AGENT+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\"=\""+REL_AGENT+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_MATRIX+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\"=\""+REL_MATRIX+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+ATT_LITERATUREID+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"Referenz\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_MISCID+"\" )AS \""+ATT_MISCID+"\",\n"
			+"\n"
			+"    GROUP_CONCAT(\n"
			+"        CONCAT(\n"
			+"            \""+ATT_DESCRIPTION+"\",\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_UNIT+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( '(', \""+ATT_UNIT+"\", ')' )\n"
			+"            END,\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VALUE+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( ':', \""+ATT_VALUE+"\" )\n"
			+"            END\n"
			+"        )\n"
			+"    )AS \""+ATT_MISC+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_MISCPARAM+"\"\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"MiscView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"MiscView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_TIME+"\" )AS \""+ATT_TIME+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_LOG10N+"\" )AS \""+ATT_LOG10N+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_DATA+"\"\n"
			+"    WHERE NOT( \""+ATT_TIME+"\" IS NULL OR \""+ATT_LOG10N+"\" IS NULL )\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"DataView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"DataView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"WHERE \""+ATT_TIME+"\" IS NOT NULL\n";
	
	private static final String queryXmlDoc = "SELECT\n"
			+"\n"
			+"    \""+ATT_CONDITION_MISCPARAM+"\".\""+REL_CONDITION+"\" AS \""+ATT_CONDITIONID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\"ID\" AS \""+ATT_MISCID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\""+ATT_PARAMID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\""+ATT_DESCRIPTION+"\",\n"
			+"    \""+REL_UNIT+"\".\""+ATT_UNIT+"\",\n"
			+"    \""+REL_DOUBLE+"Einfach\".\""+ATT_VALUE+"\"\n"
			+"\n"
			+"FROM \""+ATT_CONDITION_MISCPARAM+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_UNIT+"\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+ATT_UNIT+"\"=\""+REL_UNIT+"\".\"ID\"\n"
			+"\n"
			+"JOIN \""+REL_MISCPARAM+"\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+REL_MISCPARAM+"\"=\""+REL_MISCPARAM+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+ATT_VALUE+"\"=\""+REL_DOUBLE+"Einfach\".\"ID\"\n";
	
	private static final String queryModelView = "SELECT\n"
			+"\n"
			+"\""+REL_MODEL+"\".\""+ATT_FORMULA+"\",\n"
			+"\""+REL_MODEL+"\".\"ID\" AS \""+ATT_MODELID+"\",\n"
			+"\"P\".\""+ATT_PARAMNAME+"\",\n"
			+"\"D\".\""+ATT_PARAMNAME+"\" AS \""+ATT_DEP+"\",\n"
			+"\"I\".\""+ATT_PARAMNAME+"\" AS \""+ATT_INDEP+"\",\n"
			+"\""+REL_MODEL+"\".\""+ATT_NAME+"\",\n"
			+"\""+ATT_MINVALUE+"\",\n"
			+"\""+ATT_MAXVALUE+"\",\n"
			+"\""+ATT_MININDEP+"\",\n"
			+"\""+ATT_MAXINDEP+"\",\n"
			+"\""+ATT_LITERATUREID+"\",\n"
			+"\""+ATT_LITERATURETEXT+"\",\n"
			+"\""+REL_MODEL+"\".\""+ATT_LEVEL+"\",\n"
			+"\""+REL_MODEL+"\".\"Klasse\"\n"
			+"\n"
			+"FROM \""+REL_MODEL+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"    SELECT\n"
			+"        \""+ATT_MODELID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_LITERATUREID+"\".\"ID\" )AS \""+ATT_LITERATUREID+"\",\n"
			+"        GROUP_CONCAT( CONCAT( \""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_YEAR+"\" ) )AS \""+ATT_LITERATURETEXT+"\"\n"
			+"    FROM \""+REL_MODEL_LITERATURE+"\"\n"
			+"    JOIN \""+ATT_LITERATUREID+"\"\n"
			+"    ON \""+REL_MODEL_LITERATURE+"\".\""+ATT_LITERATUREID+"\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"    GROUP BY \""+ATT_MODELID+"\"\n"
			+")AS \"LitView\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"LitView\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"    SELECT \""+ATT_MODELID+"\", \""+ATT_PARAMNAME+"\"\n"
			+"    FROM \""+REL_PARAM+"\"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=3 )AS \"D\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"D\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"    SELECT\n"
			+"        \""+ATT_MODELID+"\",\n"
			+"        ARRAY_AGG( \""+ATT_PARAMNAME+"\" )AS \""+ATT_PARAMNAME+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MIN+"\" )AS \""+ATT_MININDEP+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MAX+"\" )AS \""+ATT_MAXINDEP+"\"\n"
			+"    FROM \""+REL_PARAM+"\"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=1\n"
			+"    GROUP BY \""+ATT_MODELID+"\" )AS \"I\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"I\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"    SELECT\n"
			+"        \""+ATT_MODELID+"\",\n"
			+"        ARRAY_AGG( \""+ATT_PARAMNAME+"\" )AS \""+ATT_PARAMNAME+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MIN+"\" )AS \""+ATT_MINVALUE+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MAX+"\" )AS \""+ATT_MAXVALUE+"\"\n"
			+"    FROM \""+REL_PARAM+"\"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=2\n"
			+"    GROUP BY \""+ATT_MODELID+"\" )AS \"P\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"P\".\""+ATT_MODELID+"\"\n";
	
	
	private static final String queryPei = "SELECT\n"
			+"\n"
			+"    \"MicrobialDataView\".\""+ATT_CONDITIONID+"\",\n"
			+"    \"MicrobialDataView\".\"CombaseID\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MISCID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MISC+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_TEMPERATURE+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_PH+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AW+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTNAME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXNAME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXDETAIL+"\",    \n"
			+"    \"MicrobialDataView\".\""+ATT_TIME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LOG10N+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_COMMENT+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LITERATUREID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LITERATURETEXT+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_FORMULA+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_DEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_INDEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_PARAMNAME+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_VALUE+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_NAME+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MODELID+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_ESTMODELID+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_RMS+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_RSQUARED+"\",\n"
			+"    \"EstModelPrimView\".\"AIC\",\n"
			+"    \"EstModelPrimView\".\"BIC\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MIN+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MAX+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MININDEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MAXINDEP+"\",\n"
			+"    \"EstModelPrimView\".\"LitMID\",\n"
			+"    \"EstModelPrimView\".\"LitM\",\n"
			+"    \"EstModelPrimView\".\"LitEmID\",\n"
			+"    \"EstModelPrimView\".\"LitEm\",\n"
			+"    \"EstModelPrimView\".\""+ATT_STANDARDERROR+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_VARMAPTO+"\"\n"
			+"\n"
			+"FROM(\n"
			+"\n"
			+"SELECT\n"
			+"\n"
			+"    \""+VIEW_CONDITION+"\".\"ID\" AS \""+ATT_CONDITIONID+"\",\n"
			+"    \""+REL_COMBASE+"\".\"CombaseID\",\n"
			+"    \"MiscView\".\""+ATT_MISCID+"\",\n"
			+"    \"MiscView\".\""+ATT_MISC+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_TEMPERATURE+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_PH+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AW+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\",\n"
			+"    \""+REL_AGENT+"\".\""+ATT_AGENTNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\",\n"
			+"    \""+REL_MATRIX+"\".\""+ATT_MATRIXNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXDETAIL+"\",\n"
			+"    \"DataView\".\""+ATT_TIME+"\",\n"
			+"    \"DataView\".\""+ATT_LOG10N+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_COMMENT+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\"Referenz\" AS \""+ATT_LITERATUREID+"\",\n"
			+"    CONCAT( \""+ATT_LITERATUREID+"\".\""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_LITERATUREID+"\".\""+ATT_YEAR+"\" )AS \""+ATT_LITERATURETEXT+"\"\n"
			+"\n"
			+"FROM \""+VIEW_CONDITION+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_COMBASE+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\""+REL_COMBASE+"\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_AGENT+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\"=\""+REL_AGENT+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_MATRIX+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\"=\""+REL_MATRIX+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+ATT_LITERATUREID+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"Referenz\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_MISCID+"\" )AS \""+ATT_MISCID+"\",\n"
			+"\n"
			+"    GROUP_CONCAT(\n"
			+"        CONCAT(\n"
			+"            \""+ATT_DESCRIPTION+"\",\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_UNIT+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( '(', \""+ATT_UNIT+"\", ')' )\n"
			+"            END,\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VALUE+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( ':', \""+ATT_VALUE+"\" )\n"
			+"            END\n"
			+"        )\n"
			+"    )AS \""+ATT_MISC+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_MISCPARAM+"\"\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"MiscView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"MiscView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_TIME+"\" )AS \""+ATT_TIME+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_LOG10N+"\" )AS \""+ATT_LOG10N+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_DATA+"\"\n"
			+"    WHERE NOT( \""+ATT_TIME+"\" IS NULL OR \""+ATT_LOG10N+"\" IS NULL )\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"DataView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"DataView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"WHERE \""+ATT_TIME+"\" IS NOT NULL\n"
			+"\n"
			+")\"MicrobialDataView\"\n"
			+"\n"
			+"RIGHT JOIN \"EstModelPrimView\"\n"
			+"ON \"EstModelPrimView\".\""+ATT_CONDITIONID+"\"=\"MicrobialDataView\".\""+ATT_CONDITIONID+"\"\n";	
	private static final String querySei = "WITH \""+REL_DOUBLE+"Einfach\" AS(\n"
			+"\n"
			+"SELECT\n"
			+"\n"
			+"    \"ID\",\n"
			+"\n"
			+"    CASE\n"
			+"        WHEN \""+ATT_VALUE+"\" IS NULL\n"
			+"        THEN CASE\n"
			+"            WHEN \"Minimum\" IS NULL\n"
			+"            THEN \"Maximum\"\n"
			+"            ELSE CASE\n"
			+"                WHEN \"Maximum\" IS NULL\n"
			+"                THEN \"Minimum\"\n"
			+"                ELSE ( \"Minimum\"+\"Maximum\" )/2\n"
			+"            END\n"
			+"        END\n"
			+"        ELSE \""+ATT_VALUE+"\"\n"
			+"    END AS \""+ATT_VALUE+"\"\n"
			+"\n"
			+"FROM(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        ID,\n"
			+"        CASE\n"
			+"            WHEN \"Exponent\" IS NULL\n"
			+"            THEN \""+ATT_VALUE+"\"\n"
			+"            ELSE CASE\n"
			+"                WHEN \""+ATT_VALUE+"\" IS NULL\n"
			+"                THEN POWER( 10, \"Exponent\" )\n"
			+"                ELSE \""+ATT_VALUE+"\"*POWER( 10, \"Exponent\" )\n"
			+"            END\n"
			+"        END AS \""+ATT_VALUE+"\",\n"
			+"\n"
			+"        CASE\n"
			+"            WHEN \"Minimum_exp\" IS NULL\n"
			+"            THEN \"Minimum\"\n"
			+"            ELSE CASE\n"
			+"                WHEN \"Minimum\" IS NULL\n"
			+"                THEN POWER( 10, \"Minimum_exp\" )\n"
			+"                ELSE \"Minimum\"*POWER( 10, \"Minimum_exp\" )\n"
			+"            END\n"
			+"        END AS \"Minimum\",\n"
			+"\n"
			+"        CASE\n"
			+"            WHEN \"Maximum_exp\" IS NULL\n"
			+"            THEN \"Maximum\"\n"
			+"            ELSE CASE\n"
			+"                WHEN \"Maximum\" IS NULL\n"
			+"                THEN POWER( 10, \"Maximum_exp\" )\n"
			+"                ELSE \"Maximum\"*POWER( 10, \"Maximum_exp\" )\n"
			+"            END\n"
			+"        END AS \"Maximum\"\n"
			+"\n"
			+"    FROM \""+REL_DOUBLE+"\"\n"
			+")\n"
			+"),\n"
			+"\""+VIEW_MISCPARAM+"\" AS(\n"
			+"\n"
			+"SELECT\n"
			+"\n"
			+"    \""+ATT_CONDITION_MISCPARAM+"\".\""+REL_CONDITION+"\" AS \""+ATT_CONDITIONID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\"ID\" AS \""+ATT_MISCID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\""+ATT_PARAMID+"\",\n"
			+"    \""+REL_MISCPARAM+"\".\""+ATT_DESCRIPTION+"\",\n"
			+"    \""+REL_UNIT+"\".\""+ATT_UNIT+"\",\n"
			+"    \""+REL_DOUBLE+"Einfach\".\""+ATT_VALUE+"\"\n"
			+"\n"
			+"FROM \""+ATT_CONDITION_MISCPARAM+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_UNIT+"\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+ATT_UNIT+"\"=\""+REL_UNIT+"\".\"ID\"\n"
			+"\n"
			+"JOIN \""+REL_MISCPARAM+"\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+REL_MISCPARAM+"\"=\""+REL_MISCPARAM+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\"\n"
			+"ON \""+ATT_CONDITION_MISCPARAM+"\".\""+ATT_VALUE+"\"=\""+REL_DOUBLE+"Einfach\".\"ID\"\n"
			+"),\n"
			+"\""+VIEW_DATA+"\" AS(\n"
			+"SELECT\n"
			+"\n"
			+"    \"ID\",\n"
			+"    \""+REL_CONDITION+"\" AS \""+ATT_CONDITIONID+"\",\n"
			+"\n"
			+"    CASE\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Stunde'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Minute'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"/60\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Sekunde'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"/3600\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Tag'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"*24\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Woche'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"*168\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE 'Monat'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"*730.5\n"
			+"        WHEN \""+REL_DATA+"\".\""+ATT_TIMEUNIT+"\" LIKE '"+ATT_YEAR+"'\n"
			+"        THEN \"T\".\""+ATT_VALUE+"\"*8766\n"
			+"        ELSE NULL\n"
			+"    END AS \""+ATT_TIME+"\",\n"
			+"\n"
			+"    CASE\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", 'log(10)?.*( pro |/)25(g|m[lL])' )\n"
			+"        THEN \"K\".\""+ATT_VALUE+"\"-LOG10( 25 )\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", 'log(10)?.*( pro |/)(kg|[lL])' )\n"
			+"        THEN \"K\".\""+ATT_VALUE+"\"-3\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", 'log(10)?.*( pro |/)100(g|m[lL])' )\n"
			+"        THEN \"K\".\""+ATT_VALUE+"\"-2\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", 'log(10)?.*( pro |/)0\\.1(g|m[lL])' )\n"
			+"        THEN \"K\".\""+ATT_VALUE+"\"+1\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", 'log(10)?.*( pro |/)(g|m[lL])' )\n"
			+"        THEN \"K\".\""+ATT_VALUE+"\"    \n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", '.*( pro |/)25(g|m[lL])' )\n"
			+"        THEN LOG10( \"K\".\""+ATT_VALUE+"\"/25 )\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", '.*( pro |/)(kg|[lL])' )\n"
			+"        THEN LOG10( \"K\".\""+ATT_VALUE+"\" )-3\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", '.*( pro |/)100(g|m[lL])' )\n"
			+"        THEN LOG10( \"K\".\""+ATT_VALUE+"\" )-2\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", '.*( pro |/)0\\.1(g|m[lL])' )\n"
			+"        THEN LOG10( \"K\".\""+ATT_VALUE+"\" )+1\n"
			+"        WHEN REGEXP_MATCHES( \""+REL_UNIT+"\".\""+ATT_UNIT+"\", '.*( pro |/)(g|m[lL])' )\n"
			+"        THEN CASE\n"
			+"            WHEN \"K\".\""+ATT_VALUE+"\" <= 1\n"
			+"            THEN 0\n"
			+"            ELSE LOG10( \"K\".\""+ATT_VALUE+"\" )\n"
			+"        END\n"
			+"        ELSE NULL\n"
			+"    END AS \""+ATT_LOG10N+"\",\n"
			+"\n"
			+"    \"C\".\""+ATT_VALUE+"\" AS \""+ATT_TEMPERATURE+"\",\n"
			+"    \"P\".\""+ATT_VALUE+"\" AS \""+ATT_PH+"\",\n"
			+"    \"A\".\""+ATT_VALUE+"\" AS \""+ATT_AW+"\",\n"
			+"    \"Q\".\""+ATT_VALUE+"\" AS \"Druck\",\n"
			+"    \"R\".\""+ATT_VALUE+"\" AS \"CO2\",\n"
			+"    \"S\".\""+ATT_VALUE+"\" AS \"Luftfeuchtigkeit\",\n"
			+"    \""+REL_DATA+"\".\""+ATT_MISC+"\",\n"
			+"    \""+REL_DATA+"\".\""+ATT_COMMENT+"\"\n"
			+"\n"
			+"FROM \""+REL_DATA+"\"\n"
			+"\n"
			+"JOIN \""+REL_DOUBLE+"Einfach\" AS \"T\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_TIME+"\"=\"T\".\"ID\"\n"
			+"\n"
			+"JOIN \""+REL_DOUBLE+"Einfach\" AS \"K\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_LOG10N+"\"=\"K\".\"ID\"\n"
			+"\n"
			+"JOIN \""+REL_UNIT+"\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_LOG10NUNIT+"\"=\""+REL_UNIT+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"C\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_TEMPERATURE+"\"=\"C\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"P\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_PH+"\"=\"P\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"A\"\n"
			+"ON \""+REL_DATA+"\".\""+ATT_AW+"\"=\"A\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"Q\"\n"
			+"ON \""+REL_DATA+"\".\"Druck\"=\"Q\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"R\"\n"
			+"ON \""+REL_DATA+"\".\"CO2\"=\"R\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"S\"\n"
			+"ON \""+REL_DATA+"\".\"Luftfeuchtigkeit\"=\"S\".\"ID\"\n"
			+"\n"
			+"WHERE \"Delta\" IS NULL OR NOT \"Delta\"\n"
			+"),\n"
			+"\""+VIEW_CONDITION+"\" AS(\n"
			+"SELECT\n"
			+"\n"
			+"    \""+REL_CONDITION+"\".\"ID\",\n"
			+"    \""+REL_CONDITION+"\".\"Referenz\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_AGENTID+"\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_MATRIXID+"\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_MATRIXDETAIL+"\",\n"
			+"    \"C\".\""+ATT_VALUE+"\" AS \""+ATT_TEMPERATURE+"\",\n"
			+"    \"P\".\""+ATT_VALUE+"\" AS \""+ATT_PH+"\",\n"
			+"    \"A\".\""+ATT_VALUE+"\" AS \""+ATT_AW+"\",\n"
			+"    \"O\".\""+ATT_VALUE+"\" AS \"CO2\",\n"
			+"    \"D\".\""+ATT_VALUE+"\" AS \"Druck\",\n"
			+"    \"L\".\""+ATT_VALUE+"\" AS \"Luftfeuchtigkeit\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_MISC+"\",\n"
			+"    \""+REL_CONDITION+"\".\""+ATT_COMMENT+"\"\n"
			+"\n"
			+"FROM\n"
			+"\n"
			+"\""+REL_CONDITION+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"C\"\n"
			+"ON \""+REL_CONDITION+"\".\""+ATT_TEMPERATURE+"\"=\"C\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"P\"\n"
			+"ON \""+REL_CONDITION+"\".\""+ATT_PH+"\"=\"P\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"A\"\n"
			+"ON \""+REL_CONDITION+"\".\""+ATT_AW+"\"=\"A\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"O\"\n"
			+"ON \""+REL_CONDITION+"\".\"CO2\"=\"O\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"D\"\n"
			+"ON \""+REL_CONDITION+"\".\"Druck\"=\"D\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_DOUBLE+"Einfach\" AS \"L\"\n"
			+"ON \""+REL_CONDITION+"\".\"Luftfeuchtigkeit\"=\"L\".\"ID\"\n"
			+"),\n"
			+"\"MicrobialDataView\" AS(\n"
			+"SELECT\n"
			+"\n"
			+"    \""+VIEW_CONDITION+"\".\"ID\" AS \""+ATT_CONDITIONID+"\",\n"
			+"    \""+REL_COMBASE+"\".\"CombaseID\",\n"
			+"    \"MiscView\".\""+ATT_MISCID+"\",\n"
			+"    \"MiscView\".\""+ATT_MISC+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_TEMPERATURE+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_PH+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AW+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\",\n"
			+"    \""+REL_AGENT+"\".\""+ATT_AGENTNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\",\n"
			+"    \""+REL_MATRIX+"\".\""+ATT_MATRIXNAME+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_MATRIXDETAIL+"\",\n"
			+"    \"DataView\".\""+ATT_TIME+"\",\n"
			+"    \"DataView\".\""+ATT_LOG10N+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\""+ATT_COMMENT+"\",\n"
			+"    \""+VIEW_CONDITION+"\".\"Referenz\" AS \""+ATT_LITERATUREID+"\",\n"
			+"    CONCAT( \""+ATT_LITERATUREID+"\".\""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_LITERATUREID+"\".\""+ATT_YEAR+"\" )AS \""+ATT_LITERATURETEXT+"\"\n"
			+"\n"
			+"FROM \""+VIEW_CONDITION+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_COMBASE+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\""+REL_COMBASE+"\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_AGENT+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_AGENTID+"\"=\""+REL_AGENT+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_MATRIX+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\""+ATT_MATRIXID+"\"=\""+REL_MATRIX+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN \""+ATT_LITERATUREID+"\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"Referenz\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_MISCID+"\" )AS \""+ATT_MISCID+"\",\n"
			+"\n"
			+"    GROUP_CONCAT(\n"
			+"        CONCAT(\n"
			+"            \""+ATT_DESCRIPTION+"\",\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_UNIT+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( '(', \""+ATT_UNIT+"\", ')' )\n"
			+"            END,\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VALUE+"\" IS NULL\n"
			+"                THEN ''\n"
			+"                ELSE CONCAT( ':', \""+ATT_VALUE+"\" )\n"
			+"            END\n"
			+"        )\n"
			+"    )AS \""+ATT_MISC+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_MISCPARAM+"\"\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"MiscView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"MiscView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+ATT_CONDITIONID+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_TIME+"\" )AS \""+ATT_TIME+"\",\n"
			+"        GROUP_CONCAT( \""+ATT_LOG10N+"\" )AS \""+ATT_LOG10N+"\"\n"
			+"\n"
			+"    FROM \""+VIEW_DATA+"\"\n"
			+"    WHERE NOT( \""+ATT_TIME+"\" IS NULL OR \""+ATT_LOG10N+"\" IS NULL )\n"
			+"    GROUP BY \""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+")\"DataView\"\n"
			+"ON \""+VIEW_CONDITION+"\".\"ID\"=\"DataView\".\""+ATT_CONDITIONID+"\"\n"
			+"\n"
			+"WHERE \""+ATT_TIME+"\" IS NOT NULL\n"
			+"),\n"
			+"\"EstModelPrimView\" AS(\n"
			+"SELECT\n"
			+"    \""+ATT_FORMULA+"\",\n"
			+"    \""+ATT_DEP+"\",\n"
			+"    \""+ATT_INDEP+"\",\n"
			+"    \""+ATT_PARAMNAME+"\",\n"
			+"    \""+ATT_VALUE+"\",\n"
			+"    \""+ATT_NAME+"\",\n"
			+"    \""+REL_MODEL+"\".\"ID\" AS \""+ATT_MODELID+"\",\n"
			+"    \""+REL_ESTMODEL+"\".\"ID\" AS \""+ATT_ESTMODELID+"\",\n"
			+"    \""+ATT_RMS+"\",\n"
			+"    \""+ATT_RSQUARED+"\",\n"
			+"    \"AIC\",\n"
			+"    \"BIC\",\n"
			+"    \""+ATT_MIN+"\",\n"
			+"    \""+ATT_MAX+"\",\n"
			+"    \""+ATT_MININDEP+"\",\n"
			+"    \""+ATT_MAXINDEP+"\",\n"
			+"    \"LitMID\",\n"
			+"    \"LitM\",\n"
			+"    \"LitEmID\",\n"
			+"    \"LitEm\",\n"
			+"    \""+ATT_CONDITIONID+"\",\n"
			+"    \""+ATT_STANDARDERROR+"\",\n"
			+"    \""+ATT_VARMAPTO+"\"\n"
			+"\n"
			+"\n"
			+"FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_MODEL+"\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\""+REL_ESTMODEL+"\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_DEP+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \""+REL_MODEL+"\"\n"
			+"    ON \""+REL_MODEL+"\".\"ID\"=\""+REL_ESTMODEL+"\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_PARAM+"\".\""+ATT_MODELID+"\"=\""+REL_MODEL+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=3\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"DepVarView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"DepVarView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        ARRAY_AGG(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_INDEP+"\",\n"
			+"        ARRAY_AGG( \"Gueltig_von\" )AS \""+ATT_MININDEP+"\",\n"
			+"        ARRAY_AGG( \"Gueltig_bis\" )AS \""+ATT_MAXINDEP+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \"GueltigkeitsBereiche\"\n"
			+"    ON \""+REL_ESTMODEL+"\".\"ID\"=\"GueltigkeitsBereiche\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \"GueltigkeitsBereiche\".\""+ATT_PARAMID+"\"=\""+REL_PARAM+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=1\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"IndepVarView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"IndepVarView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        ARRAY_AGG(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_PARAMNAME+"\",\n"
			+"        ARRAY_AGG( \""+ATT_VALUE+"\" )AS \""+ATT_VALUE+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MIN+"\" )AS \""+ATT_MIN+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MAX+"\" )AS \""+ATT_MAX+"\",\n"
			+"        ARRAY_AGG( \""+ATT_STANDARDERROR+"\" )AS \""+ATT_STANDARDERROR+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \""+REL_ESTPARAM+"\"\n"
			+"    ON \""+REL_ESTMODEL+"\".\"ID\"=\""+REL_ESTPARAM+"\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_ESTPARAM+"\".\""+ATT_PARAMID+"\"=\""+REL_PARAM+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=2\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"ParamView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"ParamView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN (\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_MODELID+"\",\n"
			+"        GROUP_CONCAT( CONCAT( \""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_YEAR+"\" ) )AS \"LitM\",\n"
			+"        GROUP_CONCAT( \""+ATT_LITERATUREID+"\".\"ID\" )AS \"LitMID\"\n"
			+"    FROM \""+REL_MODEL_LITERATURE+"\"\n"
			+"    JOIN \""+ATT_LITERATUREID+"\"\n"
			+"    ON \""+REL_MODEL_LITERATURE+"\".\""+ATT_LITERATUREID+"\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"    GROUP BY \""+ATT_MODELID+"\"\n"
			+"\n"
			+")\"LitMView\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"LitMView\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN (\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT( CONCAT( \""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_YEAR+"\" ) )AS \"LitEm\",\n"
			+"        GROUP_CONCAT( \""+ATT_LITERATUREID+"\".\"ID\" )AS \"LitEmID\"\n"
			+"\n"
			+"    FROM \""+ATT_ESTMODELID+"_Referenz\"\n"
			+"\n"
			+"    JOIN \""+ATT_LITERATUREID+"\"\n"
			+"    ON \""+ATT_ESTMODELID+"_Referenz\".\""+ATT_LITERATUREID+"\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+")\"LitEmView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"LitEmView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT(\n"
			+"            CONCAT(\n"
			+"                \""+REL_VARMAP+"\".\""+ATT_VARMAPTO+"\",\n"
			+"                '=',\n"
			+"                \""+REL_PARAM+"\".\""+ATT_PARAMNAME+"\" ) )AS \""+ATT_VARMAPTO+"\"\n"
			+"\n"
			+"    FROM \""+REL_VARMAP+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_PARAM+"\".\"ID\"=\""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\" \n"
			+"\n"
			+"    GROUP BY \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\""+ATT_VARMAPTO+"View\"\n"
			+"ON \""+ATT_VARMAPTO+"View\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"WHERE \""+ATT_LEVEL+"\"=1\n"
			+"),\n"
			+"\"PeiView\" AS(\n"
			+"SELECT\n"
			+"    \"MicrobialDataView\".\""+ATT_CONDITIONID+"\",\n"
			+"    \"MicrobialDataView\".\"CombaseID\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MISCID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MISC+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_TEMPERATURE+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_PH+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AW+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTNAME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_AGENTDETAIL+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXNAME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_MATRIXDETAIL+"\",    \n"
			+"    \"MicrobialDataView\".\""+ATT_TIME+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LOG10N+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_COMMENT+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LITERATUREID+"\",\n"
			+"    \"MicrobialDataView\".\""+ATT_LITERATURETEXT+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_FORMULA+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_DEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_INDEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_PARAMNAME+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_VALUE+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_NAME+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MODELID+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_ESTMODELID+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_RMS+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_RSQUARED+"\",\n"
			+"    \"EstModelPrimView\".\"AIC\",\n"
			+"    \"EstModelPrimView\".\"BIC\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MIN+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MAX+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MININDEP+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_MAXINDEP+"\",\n"
			+"    \"EstModelPrimView\".\"LitMID\",\n"
			+"    \"EstModelPrimView\".\"LitM\",\n"
			+"    \"EstModelPrimView\".\"LitEmID\",\n"
			+"    \"EstModelPrimView\".\"LitEm\",\n"
			+"    \"EstModelPrimView\".\""+ATT_STANDARDERROR+"\",\n"
			+"    \"EstModelPrimView\".\""+ATT_VARMAPTO+"\"\n"
			+"\n"
			+"FROM \"MicrobialDataView\"\n"
			+"\n"
			+"RIGHT JOIN \"EstModelPrimView\"\n"
			+"ON \"EstModelPrimView\".\""+ATT_CONDITIONID+"\"=\"MicrobialDataView\".\""+ATT_CONDITIONID+"\"\n"
			+"),\n"
			+"\"EstModelSecView\" AS(\n"
			+"SELECT\n"
			+"    \""+ATT_FORMULA+"\" AS \""+ATT_FORMULA+"2\",\n"
			+"    \""+ATT_DEP+"\" AS \""+ATT_DEP+"2\",\n"
			+"    \""+ATT_INDEP+"\" AS \""+ATT_INDEP+"2\",\n"
			+"    \""+ATT_PARAMNAME+"\" AS \""+ATT_PARAMNAME+"2\",\n"
			+"    \""+ATT_VALUE+"\" AS \""+ATT_VALUE+"2\",\n"
			+"    \""+ATT_NAME+"\" AS \""+ATT_NAME+"2\",\n"
			+"    \""+REL_MODEL+"\".\"ID\" AS \""+ATT_MODELID+"2\",\n"
			+"    \""+REL_ESTMODEL+"\".\"ID\" AS \""+ATT_ESTMODELID+"2\",\n"
			+"    \""+ATT_RMS+"\" AS \""+ATT_RMS+"2\",\n"
			+"    \""+ATT_RSQUARED+"\" AS \""+ATT_RSQUARED+"2\",\n"
			+"    \"AIC\" AS \"AIC2\",\n"
			+"    \"BIC\" AS \"BIC2\",\n"
			+"    \""+ATT_MIN+"\" AS \""+ATT_MIN+"2\",\n"
			+"    \""+ATT_MAX+"\" AS \""+ATT_MAX+"2\",\n"
			+"    \""+ATT_MININDEP+"\" AS \""+ATT_MININDEP+"2\",\n"
			+"    \""+ATT_MAXINDEP+"\" AS \""+ATT_MAXINDEP+"2\",\n"
			+"    \"LitMID\" AS \"LitMID2\",\n"
			+"    \"LitM\" AS \"LitM2\",\n"
			+"    \"LitEmID\" AS \"LitEmID2\",\n"
			+"    \"LitEm\" AS \"LitEm2\",\n"
			+"    \""+ATT_CONDITIONID+"\" AS \""+ATT_CONDITIONID+"2\",\n"
			+"    \""+ATT_STANDARDERROR+"\" AS \""+ATT_STANDARDERROR+"2\",\n"
			+"    \""+ATT_VARMAPTO+"\" AS \""+ATT_VARMAPTO+"2\"\n"
			+"\n"
			+"FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"LEFT JOIN \""+REL_MODEL+"\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\""+REL_ESTMODEL+"\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_DEP+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \""+REL_MODEL+"\"\n"
			+"    ON \""+REL_MODEL+"\".\"ID\"=\""+REL_ESTMODEL+"\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_PARAM+"\".\""+ATT_MODELID+"\"=\""+REL_MODEL+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=3\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"DepVarView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"DepVarView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        ARRAY_AGG(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_INDEP+"\",\n"
			+"        ARRAY_AGG( \"Gueltig_von\" )AS \""+ATT_MININDEP+"\",\n"
			+"        ARRAY_AGG( \"Gueltig_bis\" )AS \""+ATT_MAXINDEP+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \"GueltigkeitsBereiche\"\n"
			+"    ON \""+REL_ESTMODEL+"\".\"ID\"=\"GueltigkeitsBereiche\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \"GueltigkeitsBereiche\".\""+ATT_PARAMID+"\"=\""+REL_PARAM+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=1\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"IndepVarView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"IndepVarView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        ARRAY_AGG(\n"
			+"            CASE\n"
			+"                WHEN \""+ATT_VARMAPTO+"\" IS NOT NULL\n"
			+"                THEN \""+ATT_VARMAPTO+"\"\n"
			+"                ELSE \""+ATT_PARAMNAME+"\"\n"
			+"            END\n"
			+"        )AS \""+ATT_PARAMNAME+"\",\n"
			+"        ARRAY_AGG( \""+ATT_VALUE+"\" )AS \""+ATT_VALUE+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MIN+"\" )AS \""+ATT_MIN+"\",\n"
			+"        ARRAY_AGG( \""+ATT_MAX+"\" )AS \""+ATT_MAX+"\",\n"
			+"        ARRAY_AGG( \""+ATT_STANDARDERROR+"\" )AS \""+ATT_STANDARDERROR+"\"\n"
			+"\n"
			+"    FROM \""+REL_ESTMODEL+"\"\n"
			+"\n"
			+"    JOIN \""+REL_ESTPARAM+"\"\n"
			+"    ON \""+REL_ESTMODEL+"\".\"ID\"=\""+REL_ESTPARAM+"\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_ESTPARAM+"\".\""+ATT_PARAMID+"\"=\""+REL_PARAM+"\".\"ID\"\n"
			+"\n"
			+"    LEFT JOIN \""+REL_VARMAP+"\"\n"
			+"    ON \""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"=\""+REL_PARAM+"\".\"ID\" AND \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"    WHERE \""+ATT_PARAMTYPE+"\"=2\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\"ParamView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"ParamView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN (\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_MODELID+"\",\n"
			+"        GROUP_CONCAT( CONCAT( \""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_YEAR+"\" ) )AS \"LitM\",\n"
			+"        GROUP_CONCAT( \""+ATT_LITERATUREID+"\".\"ID\" )AS \"LitMID\"\n"
			+"    FROM \""+REL_MODEL_LITERATURE+"\"\n"
			+"    JOIN \""+ATT_LITERATUREID+"\"\n"
			+"    ON \""+REL_MODEL_LITERATURE+"\".\""+ATT_LITERATUREID+"\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"    GROUP BY \""+ATT_MODELID+"\"\n"
			+"\n"
			+")\"LitMView\"\n"
			+"ON \""+REL_MODEL+"\".\"ID\"=\"LitMView\".\""+ATT_MODELID+"\"\n"
			+"\n"
			+"LEFT JOIN (\n"
			+"\n"
			+"    SELECT\n"
			+"        \""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT( CONCAT( \""+ATT_FIRSTAUTHOR+"\", '_', \""+ATT_YEAR+"\" ) )AS \"LitEm\",\n"
			+"        GROUP_CONCAT( \""+ATT_LITERATUREID+"\".\"ID\" )AS \"LitEmID\"\n"
			+"\n"
			+"    FROM \""+ATT_ESTMODELID+"_Referenz\"\n"
			+"\n"
			+"    JOIN \""+ATT_LITERATUREID+"\"\n"
			+"    ON \""+ATT_ESTMODELID+"_Referenz\".\""+ATT_LITERATUREID+"\"=\""+ATT_LITERATUREID+"\".\"ID\"\n"
			+"\n"
			+"    GROUP BY \""+ATT_ESTMODELID+"\"\n"
			+")\"LitEmView\"\n"
			+"ON \""+REL_ESTMODEL+"\".\"ID\"=\"LitEmView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"LEFT JOIN(\n"
			+"\n"
			+"    SELECT\n"
			+"\n"
			+"        \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\",\n"
			+"        GROUP_CONCAT(\n"
			+"            CONCAT(\n"
			+"                \""+REL_VARMAP+"\".\""+ATT_VARMAPTO+"\",\n"
			+"                '=',\n"
			+"                \""+REL_PARAM+"\".\""+ATT_PARAMNAME+"\" ) )AS \""+ATT_VARMAPTO+"\"\n"
			+"\n"
			+"    FROM \""+REL_VARMAP+"\"\n"
			+"\n"
			+"    JOIN \""+REL_PARAM+"\"\n"
			+"    ON \""+REL_PARAM+"\".\"ID\"=\""+REL_VARMAP+"\".\""+ATT_VARMAPFROM+"\"\n"
			+"\n"
			+"    GROUP BY \""+REL_VARMAP+"\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+")\""+ATT_VARMAPTO+"View\"\n"
			+"ON \""+ATT_VARMAPTO+"View\".\""+ATT_ESTMODELID+"\"=\""+REL_ESTMODEL+"\".\"ID\"\n"
			+"\n"
			+"WHERE \""+ATT_LEVEL+"\"=2\n"
			+"),\n"
			+"\"SeiView\" AS(\n"
			+"SELECT *\n"
			+"\n"
			+"FROM\n"
			+"\n"
			+"    \"PeiView\"\n"
			+"\n"
			+"    JOIN \"Sekundaermodelle_Primaermodelle\"\n"
			+"    ON \"Sekundaermodelle_Primaermodelle\".\"GeschaetztesPrimaermodell\"=\"PeiView\".\""+ATT_ESTMODELID+"\"\n"
			+"\n"
			+"    JOIN \"EstModelSecView\"\n"
			+"    ON \"Sekundaermodelle_Primaermodelle\".\"GeschaetztesSekundaermodell\"=\"EstModelSecView\".\""+ATT_ESTMODELID+"2\"\n"
			+"\n"
			+"ORDER BY \"EstModelSecView\".\""+ATT_ESTMODELID+"2\" ASC\n"
			+")\n"
			+"SELECT * FROM \"SeiView\"\n";
	
	
	
	public static final int PARAMTYPE_INDEP = 1;
	public static final int PARAMTYPE_PARAM = 2;
	public static final int PARAMTYPE_DEP = 3;
	

	
	
	public Bfrdb( final Connection conn ) { super( conn ); }
	
	public Bfrdb( final String filename, final String login, final String pw )
	throws ClassNotFoundException, SQLException {
		super( filename, login, pw );
	}
	
	public void close() throws SQLException {		
		//super.close();
	}
	
	public ResultSet selectModel( final int level ) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement( queryModelView+" WHERE \""+ATT_LEVEL+"\"=?" );
		ps.setInt( 1, level );
		
		return ps.executeQuery();
	}
	
	
	
	public KnimeTuple getPrimModelById( int id )throws SQLException {
		
		KnimeTuple tuple;
		PmmXmlDoc doc;
		String formula;
		
		tuple = null;
		try( PreparedStatement stat = conn.prepareStatement(
			queryModelView
			+" WHERE \""+ATT_LEVEL+"\"=1 AND \"Modellkatalog\".\"ID\"=?" ) ) {
			
			stat.setInt( 1, id );
			try( ResultSet result = stat.executeQuery() ) {
				
				if( result.next() ) {
					
		    		formula = result.getString( Bfrdb.ATT_FORMULA );
		    		if( formula != null )
						formula = formula.replaceAll( "~", "=" ).replaceAll( "\\s", "" );

					
					tuple = new KnimeTuple( new Model1Schema() );
					
					doc = new PmmXmlDoc();
					doc.add(
						new CatalogModelXml(
							result.getInt( Bfrdb.ATT_MODELID ),
							result.getString( Bfrdb.ATT_NAME ),
							formula) );
					tuple.setValue( Model1Schema.ATT_MODELCATALOG, doc );
					
		    		doc = new PmmXmlDoc();
		    		doc.add( new DepXml( result.getString( Bfrdb.ATT_DEP ) ) );
		    		tuple.setValue( Model1Schema.ATT_DEPENDENT, doc );
		    		
		    		tuple.setValue(
	    				Model1Schema.ATT_INDEPENDENT,
	    				DbIo.convertArrays2IndepXmlDoc(
    						null,
    						result.getArray( Bfrdb.ATT_INDEP ),
		    				null,
		    				null ) );
		    		
		    		tuple.setValue( Model1Schema.ATT_PARAMETER,
	    				DbIo.convertArrays2ParamXmlDoc(
    						null,
    						result.getArray( Bfrdb.ATT_PARAMNAME ),
		    				null,
		    				null,
		    				result.getArray( Bfrdb.ATT_MINVALUE ),
		    				result.getArray( Bfrdb.ATT_MAXVALUE ) ) );	
		    		
					doc = new PmmXmlDoc();
					doc.add( new EstModelXml(
						null, null, null, null, null, null, null ) );
					tuple.setValue(Model1Schema.ATT_ESTMODEL, doc);
					
					String s = result.getString( Bfrdb.ATT_LITERATUREID );
		    		if (s != null)
						tuple.setValue(Model1Schema.ATT_MLIT, getLiterature(  s ) );
					
		    		tuple.setValue( Model1Schema.ATT_DATABASEWRITABLE, Model1Schema.WRITABLE );
		    		tuple.setValue( Model1Schema.ATT_DBUUID, getDBUUID() );
				}
				
			}
		}
		
		return tuple;
	}
	
	public KnimeTuple getSecModelById( int id )throws SQLException {
		
		KnimeTuple tuple;
		PmmXmlDoc doc;
		String formula;
		

		
		tuple = null;
		try( PreparedStatement stat = conn.prepareStatement(
			queryModelView
			+" WHERE \""+ATT_LEVEL+"\"=2 AND \"Modellkatalog\".\"ID\"=?" ) ) {
			
			
			
			stat.setInt( 1, id );
			try( ResultSet result = stat.executeQuery() ) {
				
				if( result.next() ) {
					
		    		formula = result.getString( Bfrdb.ATT_FORMULA );
		    		if( formula != null )
						formula = formula.replaceAll( "~", "=" ).replaceAll( "\\s", "" );

					
					tuple = new KnimeTuple( new Model2Schema() );
					
					doc = new PmmXmlDoc();
					doc.add(
						new CatalogModelXml(
							result.getInt( Bfrdb.ATT_MODELID ),
							result.getString( Bfrdb.ATT_NAME ),
							formula) );
					tuple.setValue( Model2Schema.ATT_MODELCATALOG, doc );
					
		    		doc = new PmmXmlDoc();
		    		doc.add( new DepXml( result.getString( Bfrdb.ATT_DEP ) ) );
		    		tuple.setValue( Model2Schema.ATT_DEPENDENT, doc );
		    		
		    		tuple.setValue(
	    				Model2Schema.ATT_INDEPENDENT,
	    				DbIo.convertArrays2IndepXmlDoc(
    						null,
    						result.getArray( Bfrdb.ATT_INDEP ),
		    				null,
		    				null ) );
		    		
		    		tuple.setValue( Model2Schema.ATT_PARAMETER,
	    				DbIo.convertArrays2ParamXmlDoc(
    						null,
    						result.getArray( Bfrdb.ATT_PARAMNAME ),
		    				null,
		    				null,
		    				result.getArray( Bfrdb.ATT_MINVALUE ),
		    				result.getArray( Bfrdb.ATT_MAXVALUE ) ) );	
		    		
					doc = new PmmXmlDoc();
					doc.add( new EstModelXml(
						null, null, null, null, null, null, null ) );
					tuple.setValue(Model2Schema.ATT_ESTMODEL, doc);
					
					String s = result.getString( Bfrdb.ATT_LITERATUREID );
		    		if (s != null)
						tuple.setValue(Model2Schema.ATT_MLIT, getLiterature(  s ) );
					
		    		tuple.setValue( Model2Schema.ATT_DATABASEWRITABLE, Model2Schema.WRITABLE );
		    		tuple.setValue( Model2Schema.ATT_DBUUID, getDBUUID() );
				}
				
			}
		}
		
		return tuple;
	}
	
	public PmmXmlDoc getMiscXmlDoc(Integer tsID) throws SQLException {
		
		String q;
		PmmXmlDoc miscDoc = new PmmXmlDoc();
		
		q = queryXmlDoc+" WHERE \""+REL_CONDITION+"\"="+tsID;
		// System.out.println( q );
		PreparedStatement ps = conn.prepareStatement( q );
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			MiscXml mx = new MiscXml(rs.getInt("SonstigesID"),rs.getString("Parameter"),rs.getString("Beschreibung"),rs.getDouble("Wert"),rs.getString("Einheit"));
			miscDoc.add(mx);
		}
		return miscDoc;
	}
	public ResultSet selectEstModel( final int level ) throws SQLException {
		String q;
		if( level == 1 ) {
			q = queryPei;
		} else {
			q = querySei;
		}		
		PreparedStatement ps = conn.prepareStatement( q );
		return ps.executeQuery();
	}
	
	public ResultSet selectTs() throws SQLException {	
		return pushQuery( queryTimeSeries7 );
	}
	
	public ResultSet selectRelatedLiterature( final String modelName ) throws SQLException {
		
		
		String q;
		PreparedStatement psSelectRelLit;
		
		q = "SELECT \""+ATT_LITERATUREID+"\".\""+ATT_FIRSTAUTHOR+"\", \""+ATT_LITERATUREID
		+"\".\""+ATT_YEAR+"\" FROM \""+REL_MODEL+"\" JOIN \""+REL_MODEL_LITERATURE
		+"\" ON \""+REL_MODEL+"\".\"ID\"=\""+REL_MODEL_LITERATURE
		+"\".\""+ATT_MODELID+"\" JOIN \""+ATT_LITERATUREID+"\" ON \""
		+ATT_LITERATUREID+"\".\"ID\"=\""+REL_MODEL_LITERATURE+"\".\""
		+ATT_LITERATUREID+"\" WHERE \""+ATT_NAME+"\"=?";
		
		psSelectRelLit = conn.prepareStatement( q );
		
		psSelectRelLit.setString( 1, modelName );
		
		return psSelectRelLit.executeQuery();
	}
	
	public String getRelatedLiterature( final String modelName ) {
		
		ResultSet result;
		String ret;
		
		ret = "";
		
		try {
			
			result = selectRelatedLiterature( modelName );
			
			while( result.next() ) {
				
				if( !ret.isEmpty() ) {
					ret += ", ";
				}
				
				ret += result.getString( 1 )+" et al. "+result.getString( 2 );
			}
			
			result.getStatement().close();
			result.close();
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	public HashMap<Integer, String> getPossibleLiterature() {
		
		HashMap<Integer, String> ret;
		ResultSet result;
		
		ret = new HashMap<Integer, String>();
		
		try {
			
			PreparedStatement psSelectPossLit;
			
			psSelectPossLit = conn.prepareStatement(
					
				"SELECT \"ID\",\""+ATT_FIRSTAUTHOR+"\", \""+ATT_YEAR+"\" FROM \""+ATT_LITERATUREID+"\""
			);
			
			result = psSelectPossLit.executeQuery();
			
			while( result.next() ) {
				
				ret.put(result.getInt("ID"), result.getString(2)+" et al. "+result.getInt(3) );
			}
			
			result.getStatement().close();
			result.close();
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	public int getNumLitEntry() {
		
		int n;
		PreparedStatement psNumLitEntry;
		ResultSet result;
		
		n = -1;
		
		try {
		
			psNumLitEntry = conn.prepareStatement( "SELECT COUNT( * )FROM \""+ATT_LITERATUREID+"\"" );
			
			result = psNumLitEntry.executeQuery();
			
			result.next();
			return result.getInt( 1 );
			
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		
		return n;
	}
	
	public int getNumModel() {
		
		int n;
		PreparedStatement psNumLitEntry;
		ResultSet result;
		
		n = -1;
		
		try {
		
			psNumLitEntry = conn.prepareStatement( "SELECT COUNT( * )FROM \""+REL_MODEL+"\"" );
			
			result = psNumLitEntry.executeQuery();
			
			result.next();
			return result.getInt( 1 );
			
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		
		return n;
	}
	
	public LinkedList<String> getPossibleModelName() {
		
		LinkedList<String> ret;
		String q;
		PreparedStatement psPossibleModelName;
		ResultSet result;
		
		ret = new LinkedList<String>();
		
		q = "SELECT \""+ATT_NAME+"\" FROM \""+REL_MODEL+"\"";
		
		try {
		
			psPossibleModelName = conn.prepareStatement( q );
			
			result = psPossibleModelName.executeQuery();
			
			while( result.next() ) {
				ret.add( result.getString( 1 ) );
			}
			
			result.getStatement().close();
			result.close();
			
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		return ret;                                    
	}
	
	public HashMap<Integer, List<Integer>> getModLitMatrix() {
		
		HashMap<Integer, List<Integer>> resultt = new HashMap<Integer, List<Integer>>();
		//boolean[][] mat;
		//int n, m, i, j;
		//HashMap<Integer, String> literature;
		//LinkedList<String> model;
		String q;
		PreparedStatement psLitMat;
		ResultSet result;
		
		//n = getNumLitEntry();
		//m = getNumModel();
		
		//mat = new boolean[ n ][ m ];
		
		//literature = getPossibleLiterature();
		//model = getPossibleModelName();
		
		q = "SELECT \"Modell\",\"Literatur\" FROM \""+REL_MODEL_LITERATURE+"\"";
		
		
		try {
			
			psLitMat = conn.prepareStatement( q );
			
			result = psLitMat.executeQuery();
			
			while( result.next() ) {
				
				//i = indexOf( literature, result.getString( 2 ) );
				//j = indexOf( model, result.getString( 3 ) );
				
				//mat[ i ][ j ] = true;
				List<Integer> li = resultt.get(result.getInt(1));
				if (li == null) {
					li = new ArrayList<Integer>();
				}
				li.add(result.getInt(2));
				resultt.put(result.getInt(1), li);
			}
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		return resultt;
	}
	
	public void insertEm2(final Integer secID, final List<Integer> primIDs) {
		try {
			PreparedStatement ps = conn.prepareStatement( "INSERT INTO \"Sekundaermodelle_Primaermodelle\" (\"GeschaetztesPrimaermodell\", \"GeschaetztesSekundaermodell\")VALUES(?,?)");
			for (Integer id : primIDs) {
				if (id != null && id >= 0) {
					ps.setInt( 1, id);
					ps.setInt( 2, secID);
					ps.executeUpdate();			
				}
			}
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
	}
	public Integer insertEm(final ParametricModel pm) { // , HashMap<String, String> hm
		Integer estModelId = null;

		Double rms = pm.getRms();
		Double r2 = pm.getRsquared();

			estModelId = pm.getEstModelId();
			int condId = pm.getCondId();
			int modelId = pm.getModelId();
			
			HashMap<String, Integer> hmi = new HashMap<String, Integer>(); 
			int responseId = queryParamId(modelId, pm.getDepXml().getOrigName(), PARAMTYPE_DEP);
			if (!pm.getDepXml().getOrigName().equals(pm.getDepXml().getName())) hmi.put(pm.getDepXml().getName(), responseId);

			if (responseId < 0) {
				System.err.println("responseId < 0..." + pm.getDepVar() + "\t" + pm.getDepVar());
			}

			if (isObjectPresent(REL_ESTMODEL, estModelId)) {
				updateEstModel(estModelId, condId, modelId, rms, r2, pm.getAic(), pm.getBic(), responseId);
			} else {
				estModelId = insertEstModel(condId, modelId, rms, r2, pm.getAic(), pm.getBic(), responseId);
				pm.setEstModelId(estModelId);
			}
			
			deleteFrom("GeschaetzteParameter", "GeschaetztesModell", estModelId);
			for (PmmXmlElementConvertable el : pm.getParameter().getElementSet()) {
				if (el instanceof ParamXml) {
					ParamXml px = (ParamXml) el;
					int paramId = queryParamId(modelId, px.getOrigName(), PARAMTYPE_PARAM);
					if (paramId < 0) {
						System.err.println("paramId < 0... " + px.getOrigName());
					}
					if (!px.getOrigName().equals(px.getName())) hmi.put(px.getName(), paramId);
					insertEstParam(estModelId, paramId, px.getValue(), px.getError());
				}
			}

			insertModLit(estModelId, pm.getEstModelLit(), true, pm);
			
			deleteFrom("GueltigkeitsBereiche", "GeschaetztesModell", estModelId);
			for (PmmXmlElementConvertable el : pm.getIndependent().getElementSet()) {
				if (el instanceof IndepXml) {
					IndepXml ix = (IndepXml) el;
					int indepId = queryParamId(modelId, ix.getOrigName(), PARAMTYPE_INDEP);
					if (indepId >= 0) {
						insertMinMaxIndep(estModelId, indepId, ix.getMin(), ix.getMax());	
						if (!ix.getOrigName().equals(ix.getName())) hmi.put(ix.getName(), indepId);
					}
					else {
						System.err.println("insertEm:\t" + ix.getOrigName() + "\t" + modelId);
					}
				}
			}

			// insert mapping of parameters and variables of this estimation
			deleteFrom("VarParMaps", "GeschaetztesModell", estModelId);			
			for (String newName : hmi.keySet()) {
				insertVarParMaps(estModelId, hmi.get(newName), newName);					
			}			
			
		return estModelId;
	}
	private void insertVarParMaps(final int estModelId, final int paramId, final String newVarPar) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO \"VarParMaps\" (\"GeschaetztesModell\", \"VarPar\", \"VarParMap\") VALUES (?,?,?)");
			ps.setInt(1, estModelId);
			ps.setInt(2, paramId);
			if (newVarPar == null) {
				ps.setNull(3, Types.VARCHAR);
			} else {
				ps.setString(3, newVarPar);
			}
			ps.executeUpdate();			
			ps.close();
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}		
	}
	private void insertMinMaxIndep(final int estModelId, final int paramId, final Double min, final Double max) {
		try {
			PreparedStatement ps = conn.prepareStatement( "INSERT INTO \"GueltigkeitsBereiche\" (\"GeschaetztesModell\", \"Parameter\", \"Gueltig_von\", \"Gueltig_bis\")VALUES(?,?,?,?)");
			ps.setInt( 1, estModelId);
			ps.setInt( 2, paramId);
			if (min == null) {
				ps.setNull(3, Types.DOUBLE);
			} else {
				ps.setDouble( 3, min);
			}
			if (max == null) {
				ps.setNull(4, Types.DOUBLE);
			} else {
				ps.setDouble( 4, max);
			}
			ps.executeUpdate();			
			ps.close();
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}		
	}
	
	private Integer insertCondition(Integer condId, final Integer tempId, final Integer phId, final Integer awId, final String organism,
			final String environment, final String combaseId,
			Integer matrixId, Integer agentId, final String agentDetail, final String matrixDetail, PmmXmlDoc misc, final String comment,
			PmmXmlDoc lit, PmmTimeSeries ts) {
			
		String warnings = "";
		
			boolean doUpdate = isObjectPresent("Versuchsbedingungen", condId);
			Integer cdai = combaseDataAlreadyIn(combaseId);
			if (!doUpdate && cdai != null) {
				condId = cdai;//return null;
				doUpdate = true;
			}
			Integer resultID = null;
			PreparedStatement ps;

			try {
				if (agentId == null || agentId < 0) {
					agentId = queryAgentId( organism == null ? agentDetail : organism );
				}
				if (matrixId == null || matrixId < 0) {
					matrixId = queryMatrixId( environment == null ? matrixDetail : environment );
				}
				
				if (doUpdate) {
					ps = conn.prepareStatement( "UPDATE \"Versuchsbedingungen\" SET \""+ATT_TEMPERATURE+"\"=?, \""+ATT_PH+"\"=?, \""+ATT_AW+"\"=?, \""+ATT_AGENTID+"\"=?, \"AgensDetail\"=?, \""+ATT_MATRIXID+"\"=?, \"MatrixDetail\"=?, \"b_f_details_CB\"=?, \"Kommentar\"=?, \"Referenz\"=? WHERE \"ID\"=?" );
				} else {
					ps = conn.prepareStatement( "INSERT INTO \"Versuchsbedingungen\" (\""+ATT_TEMPERATURE+"\", \""+ATT_PH+"\", \""+ATT_AW+"\", \""+ATT_AGENTID+"\", \"AgensDetail\", \""+ATT_MATRIXID+"\", \"MatrixDetail\", \"b_f_details_CB\", \"Kommentar\", \"Referenz\" ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS );
				}
				
				if( tempId >= 0 ) {
					ps.setInt( 1, tempId );
				} else {
					ps.setNull( 1, Types.INTEGER );
				}
				
				if( phId >= 0 ) {
					ps.setInt( 2, phId );
				} else {
					ps.setNull( 2, Types.INTEGER );
				}
				
				if( awId >= 0) {
					ps.setInt( 3, awId );
				} else {
					ps.setNull( 3, Types.INTEGER );
				}
				
				if (agentId == null || agentId <= 0) {
					ps.setNull( 4, Types.INTEGER );
						warnings += "Agent not defined (" + agentDetail + ")\n";						
				} else {
					ps.setInt(4, agentId );
					try {ts.setAgentId(agentId);} catch (PmmException e) {e.printStackTrace();}
				}
				if( agentDetail == null) {
					ps.setNull( 5, Types.VARCHAR );
				} else {
					ps.setString( 5, agentDetail );
				}
				if (matrixId == null || matrixId <= 0) {
					ps.setNull( 6, Types.INTEGER );
						warnings += "Matrix not defined (" + matrixDetail + ")\n";
				} else {
					ps.setInt(6, matrixId );
					try {ts.setMatrixId(matrixId);} catch (PmmException e) {e.printStackTrace();}
				}
				if( matrixDetail == null) {
					ps.setNull( 7, Types.VARCHAR );
				} else {
					ps.setString( 7, matrixDetail);
				}
				/*
				if( misc == null ) {
					ps.setNull( 8, Types.VARCHAR );
				} else {
					ps.setString( 8, misc );
				}
				*/
				ps.setNull(8, Types.VARCHAR);
				if( comment == null ) {
					ps.setNull(9, Types.VARCHAR);
				} else {
					ps.setString(9, comment);
				}
				
				insertLiteratureInCase(lit);
				List<PmmXmlElementConvertable> l = lit.getElementSet();
				if (l.size() > 0) {
					LiteratureItem li = (LiteratureItem) l.get(0);
					ps.setInt(10, li.getId());
				}
				else {
					ps.setNull(10, Types.INTEGER);					
				}
				if (doUpdate) {
					ps.setInt(11, condId);
					
					ps.executeUpdate();
					resultID = condId;
				}
				else {
					if (ps.executeUpdate() > 0) {
						ResultSet result = ps.getGeneratedKeys();
						result.next();
						resultID = result.getInt(1);
						
						result.close();
					}
					
				}
				ps.close();
			}
			catch( SQLException ex ) { ex.printStackTrace(); }
			
			if( cdai == null && resultID != null && combaseId != null && !combaseId.isEmpty()) {
				insertCondComb(resultID, combaseId);
			}
			String hcWarnings = handleConditions(resultID, misc, ts);
				warnings += hcWarnings;
			ts.setWarning(warnings);

			return resultID;
		}
	private String handleConditions(final Integer condId, final PmmXmlDoc misc, PmmTimeSeries ts) {
		String result = "";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement("DELETE FROM \"Versuchsbedingungen_Sonstiges\" WHERE \"Versuchsbedingungen\" = " + condId);
			ps.executeUpdate();
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}
		if (condId != null && condId >= 0 && misc != null) {
        	for (PmmXmlElementConvertable el : misc.getElementSet()) {
        		if (el instanceof MiscXml) {		
        			MiscXml mx = (MiscXml) el;
					String n = mx.getName();
					String d = mx.getDescription();
					if (n == null || n.isEmpty()) n = d;
					if (d == null || d.isEmpty()) d = n;
    				Integer paramID = getID("SonstigeParameter", "Beschreibung", d.toLowerCase()); // Parameter Beschreibung
    				if (paramID == null) {
						try {
							if (n != null && d != null && !n.isEmpty() && !d.isEmpty()) {
								ps = conn.prepareStatement("INSERT INTO \"SonstigeParameter\" (\"Parameter\", \"Beschreibung\") VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
								ps.setString(1, n);
								ps.setString(2, d.toLowerCase());
								if (ps.executeUpdate() > 0) {
									ResultSet rs = ps.getGeneratedKeys();
									rs.next();
									paramID = rs.getInt(1);
									rs.close();
								}
								ps.close();
							}
						}
						catch(SQLException ex) {ex.printStackTrace();}
    				}
    				if (paramID != null) {
    					//System.err.println("handleConditions:\t" + after + "\t" + dbl + "\t" + unit + "\t" + paramID + "\t" + (condIDs == null ? condIDs : condIDs.get(i)));
    					try {
    						ps = conn.prepareStatement("INSERT INTO \"Versuchsbedingungen_Sonstiges\" (\"Versuchsbedingungen\", \"SonstigeParameter\", \"Wert\", \"Einheit\", \"Ja_Nein\") VALUES (?,?,?,?,?)");
    						ps.setInt(1, condId);
    						ps.setInt(2, paramID);
    						if (mx.getValue() == null || Double.isNaN(mx.getValue())) {
    							ps.setNull(3, Types.DOUBLE);
    							ps.setNull(4, Types.INTEGER);
    							ps.setBoolean(5, true);
    						}
    						else {
    							int did = insertDouble(mx.getValue());
    							ps.setDouble(3, did);							
    							Integer eid = getID("Einheiten", "Einheit", mx.getUnit());
    							if (eid == null) {
    								ps.setNull(4, Types.INTEGER);
    							} else {
    								ps.setInt(4, eid);
    							}
    							ps.setBoolean(5, false);
    						}
    						ps.executeUpdate();
    						//try {ts.addValue(TimeSeriesSchema.ATT_MISCID, paramID);} catch (PmmException e) {e.printStackTrace();}
    					}
    					catch (Exception e) {e.printStackTrace();}
    				}
    				else {
    					//System.err.println("handleConditions, paramID not known:\t" + val + "\t" + after);
    					result += "Insert of Misc failed:\t" + mx.getName() + "\t" + mx.getDescription() + "\n";
    				}
        		}
        	}
		}
		return result;
	}
	private void insertLiteratureInCase(PmmXmlDoc lit) {
		try {
			PreparedStatement psm = conn.prepareStatement("INSERT INTO \"Literatur\" (\"Erstautor\", \"Jahr\", \"Titel\", \"Abstract\") VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			int i=0;
			for (PmmXmlElementConvertable el : lit.getElementSet()) {
				if (el instanceof LiteratureItem) {
					LiteratureItem li = (LiteratureItem) el;
					if (li.getId() <= 0) {
						if (li.getAuthor() == null) psm.setNull(1, Types.VARCHAR);
						else psm.setString(1, li.getAuthor());
						if (li.getYear() == null) psm.setNull(2, Types.INTEGER);
						else psm.setInt(2, li.getYear());
						if (li.getTitle() == null) psm.setNull(3, Types.VARCHAR);
						else psm.setString(3, li.getTitle());
						if (li.getAbstract() == null) psm.setNull(4, Types.VARCHAR);						
						else psm.setString(4, li.getAbstract());
						int newID = 0;
						try {
							if (psm.executeUpdate() > 0) {
								ResultSet result = psm.getGeneratedKeys();
								result.next();
								newID = result.getInt(1);
								result.close();
							}
						}
						catch (Exception e) {
							if (e.getMessage().startsWith("integrity constraint violation: unique")) {
								String sql = "";
								try {
									sql = "SELECT \"ID\" FROM \"Literatur\" WHERE \"Erstautor\" = '" + li.getAuthor().replace("'", "''") +
											"' AND \"Jahr\" = " + li.getYear() + " AND \"Titel\" = '" + li.getTitle().replace("'", "''") + "'";
									ResultSet result = getResultSet(sql, false);
									if(result != null && result.first()) {
										newID = result.getInt(1);
										result.close();
									}
								}
								catch(SQLException ex) {
									ex.printStackTrace();
								}
							}
						}
						if (newID > 0) {
							li.setId(newID);
							lit.set(i, li);
						}		
						else {
							MyLogger.handleMessage("insertLiteratureInCase failed... " + psm);
						}
					}
				}
				i++;
			}
			psm.close();
		}
		catch(SQLException ex) {ex.printStackTrace();}
	}
		
	private void deleteFrom(String tablename, String fieldname, int id) {
		try {			
			PreparedStatement ps = conn.prepareStatement( "DELETE FROM \""+tablename+"\" WHERE \""+fieldname+"\"="+id );			
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
	}
		
	public Integer insertTs(final PmmTimeSeries ts) throws PmmException {		
		Integer condId = ts.getCondId();
		Double ph = ts.getPh();
		Double temp = ts.getTemperature();
		Double aw = ts.getWaterActivity();
		String organism = ts.getAgentName();
		String environment = ts.getMatrixName();
		String combaseId = ts.getCombaseId();
		Integer matrixId = ts.getMatrixId();
		Integer agentId = ts.getAgentId();
		String agentDetail = ts.getAgentDetail();
		String matrixDetail = ts.getMatrixDetail();
		String comment = ts.getComment();
		PmmXmlDoc misc = ts.getMisc();
		PmmXmlDoc lit = ts.getLiterature();
		PmmXmlDoc mdData = ts.getTimeSeries();		
		
		int tempId = insertDouble(temp);
		int phId = insertDouble(ph);
		int awId = insertDouble(aw);

		condId = insertCondition(condId, tempId, phId, awId, organism, environment, combaseId,
				matrixId, agentId, agentDetail, matrixDetail, misc, comment,
				lit, ts);
			
		ts.setLiterature(lit);
		ts.setMisc(misc);
		ts.setCondId(condId);
		if( condId == null || condId < 0 ) {
			return null;
		}
		
		// delete old data
		deleteFrom("Messwerte", "Versuchsbedingungen", condId);
		for (PmmXmlElementConvertable el : mdData.getElementSet()) {
			if (el instanceof TimeSeriesXml) {
				TimeSeriesXml tsx = (TimeSeriesXml) el;
				int timeId = insertDouble(tsx.getTime());				
				int lognId = insertDouble(tsx.getLog10C());				
				insertData(condId, timeId, lognId);
			}
		}
		return condId;
	}
	
	public Integer insertM(final ParametricModel m) {		
		int modelId = m.getModelId();
		Integer fID = getId4Formula(m.getFormula());
		boolean iop = isObjectPresent("Modellkatalog", modelId); 
		
		if (iop || fID != null) {
			//Date date = new Date( System.currentTimeMillis() );		
			if (!iop) {
				modelId = fID;
				m.setModelId(modelId);
			}
			try {
				PreparedStatement ps = conn.prepareStatement( "UPDATE \"Modellkatalog\" SET \"Name\"=?, \"Level\"=?, \"Formel\"=? WHERE \"ID\"=?" );
				ps.setString(1, m.getModelName());
				ps.setInt(2, m.getLevel());
				//ps.setDate( 3, date );
				ps.setString(3, m.getFormula());
				ps.setInt(4, modelId);

				ps.executeUpdate();
				ps.close();
			}
			catch( SQLException ex ) { ex.printStackTrace(); }		
		}
		else {
			Date date = new Date( System.currentTimeMillis() );		
			
			try {				
				PreparedStatement ps = conn.prepareStatement( "INSERT INTO \"Modellkatalog\" (\"Name\", \"Level\", \"Eingabedatum\", \"Formel\", \"Notation\", \"Klasse\") VALUES( ?, ?, ?, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS );
				ps.setString( 1, m.getModelName() + "_" + (-modelId) );
				ps.setInt( 2, m.getLevel() );
				ps.setDate( 3, date );
				ps.setString( 4, m.getFormula() );
				ps.setString( 5, m.getModelName().toLowerCase().replaceAll( "\\s", "_") );
				ps.setInt(6, 0); // erstmal Unknown!
				
				modelId = -1;
				if( ps.executeUpdate() > 0) {
					ResultSet result = ps.getGeneratedKeys();
					result.next();
					modelId = result.getInt( 1 );					
					m.setModelId(modelId);
					result.close();
				}				
				ps.close();
			}
			catch(SQLException ex) {ex.printStackTrace();}

			if( modelId < 0 ) {
				return null;
			}			
		}
		
		// insert parameter set
		LinkedList<Integer> paramIdSet = new LinkedList<Integer>();
		
		// insert dependent variable
		int paramId = insertParam(modelId, m.getDepXml().getOrigName(), PARAMTYPE_DEP, null, null);
		paramIdSet.add(paramId);
		
		// insert independent variable set
		for (PmmXmlElementConvertable el : m.getIndependent().getElementSet()) {
			if (el instanceof IndepXml) {
				IndepXml ix = (IndepXml) el;
				paramId = insertParam(modelId, ix.getOrigName(), PARAMTYPE_INDEP, ix.getMin(), ix.getMax());
				paramIdSet.add(paramId);
			}
		}

		// insert parameters
		for (PmmXmlElementConvertable el : m.getParameter().getElementSet()) {
			if (el instanceof ParamXml) {
				ParamXml px = (ParamXml) el;
				paramId = insertParam(modelId, px.getOrigName(), PARAMTYPE_PARAM, px.getMin(), px.getMax());
				paramIdSet.add(paramId);
			}
		}

		insertModLit(modelId, m.getModelLit(), false, m);
		
		// delete dangling parameters
		// deleteParamNotIn kann man eigentlich nicht machen!!! Sonst sind irgendwann die Response-Verkn�pfungen weg....
		// andererseits hat man das Problem, dass sich die Parameter sammeln...
		deleteParamNotIn( modelId, paramIdSet );

		return modelId;
	}
	private void insertModLit(final int modelId, PmmXmlDoc modelLit, final boolean estimatedModels, final ParametricModel m) {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM " + (estimatedModels ? "\"GeschaetztesModell_Referenz\" WHERE \"GeschaetztesModell\"" : "\"Modell_Referenz\"WHERE \"Modell\"") + " = " + modelId);
			ps.executeUpdate();
			ps.close();
			PreparedStatement psm = conn.prepareStatement("INSERT INTO \"Modell_Referenz\" (\"Modell\", \"Literatur\") VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			PreparedStatement psgm = conn.prepareStatement("INSERT INTO \"GeschaetztesModell_Referenz\" (\"GeschaetztesModell\", \"Literatur\") VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			insertLiteratureInCase(modelLit);
			if (estimatedModels) m.setEstLit(modelLit);
			else m.setMLit(modelLit);
			for (PmmXmlElementConvertable el : modelLit.getElementSet()) {
				if (el instanceof LiteratureItem) {
					LiteratureItem li = (LiteratureItem) el;
					if (li.getId() >= 0) {
						if (!estimatedModels) {
							psm.setInt(1, modelId);
							psm.setInt(2, li.getId());
							psm.executeUpdate();
						}
						else {
							psgm.setInt(1, modelId);
							psgm.setInt(2, li.getId());
							psgm.executeUpdate();
						}
					}
				}
			}
			psm.close();
			psgm.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
	}
	private void insertCondComb(final Integer resultID, final String combaseId) {
		try {
			PreparedStatement ps = conn.prepareStatement( "INSERT INTO \"ImportedCombaseData\" (\"CombaseID\", \"Versuchsbedingung\")VALUES(?,?)" );
			ps.setString( 1, combaseId );
			ps.setInt( 2, resultID);
			ps.executeUpdate();			
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
	}
	private Integer combaseDataAlreadyIn(final String combaseId) {
		Integer res = null;
		try {
			ResultSet result = getResultSet("SELECT \"Versuchsbedingung\" FROM \"ImportedCombaseData\" WHERE \"CombaseID\" LIKE '" + combaseId + "' AND \"Versuchsbedingung\" IS NOT NULL", false);
			
			if(result != null && result.first()) {
				res = result.getInt(1);
				result.close();
			}
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		return res;
	}
	
	private int insertDouble( final Double value ) {
		
		int doubleId;
		PreparedStatement psInsertDouble;
		ResultSet result;
		
		doubleId = -1;
		try {
			psInsertDouble = conn.prepareStatement( "INSERT INTO \"DoubleKennzahlen\" (\""+ATT_VALUE+"\", \""+ATT_VALUETYPE+"\" )VALUES( ?, 1 )", Statement.RETURN_GENERATED_KEYS );
			if( value == null || Double.isNaN( value ) || Double.isInfinite( value ) ) {
				// psInsertDouble.setNull( 1, Types.DOUBLE );
				return -1;
			} else {
				psInsertDouble.setDouble( 1, value );
			}
			
			if( psInsertDouble.executeUpdate() < 1 ) {
				return doubleId;
			}
			
			result = psInsertDouble.getGeneratedKeys();
			result.next();
			doubleId = result.getInt( 1 );
			
			result.close();
			psInsertDouble.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		
		return doubleId;
	}
	
	private Integer queryAgentId( final String agentName ) {
		
		Integer agentId = null;
		PreparedStatement psQueryAgentId;
		ResultSet result;
		
		try {
			psQueryAgentId = conn.prepareStatement( "SELECT \"ID\" FROM \""+REL_AGENT+"\" WHERE \""+ATT_AGENTNAME+"\" LIKE ? OR \""+ATT_NAMESHORT+"\" LIKE ?" );
			psQueryAgentId.setString( 1, agentName );
			psQueryAgentId.setString( 2, agentName );
			
			result = psQueryAgentId.executeQuery();
			
			if( !result.next() ) {
				return agentId;
			}
			
			agentId = result.getInt( 1 );
						
			result.close();
			psQueryAgentId.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		
		return agentId;
	}
	
	
	private Integer queryMatrixId( final String matrixName ) {
		
		Integer matrixId = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement( "SELECT \"ID\" FROM \""+REL_MATRIX+"\" WHERE \""+ATT_MATRIXNAME+"\" LIKE ?" );
			ps.setString( 1, matrixName );
			
			ResultSet result = ps.executeQuery();
			
			if( !result.next() ) {
				return matrixId;
			}
			
			matrixId = result.getInt( 1 );
						
			result.close();
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		
		return matrixId;
	}
	
	private ResultSet getResultSet(final String sql, final boolean suppressWarnings) {
		    ResultSet ergebnis = null;
		    try {
		      Statement anfrage = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		      ergebnis = anfrage.executeQuery(sql);
		      ergebnis.first();
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
		    return ergebnis;
	}
	private Integer getID(final String tablename, final String feldname, final String feldVal) {
		  Integer result = null;
		  String sql = "SELECT \"ID\" FROM \"" + tablename + "\" WHERE \"" + feldname + "\"";
		  if (feldVal == null) {
			sql += " IS NULL";
		} else {
			sql += " = '" + feldVal.replace("'", "''") + "'";
		}
			ResultSet rs = getResultSet(sql, true);
			try {
				if (rs != null && rs.last()) {
					result = rs.getInt(1);
				}
			}
			catch (Exception e) {e.printStackTrace();}
			return result;
	}

	private void insertData( final int condId, final int timeId, final int lognId ) {
		
		PreparedStatement ps;
		
		try {
			
			ps = conn.prepareStatement( "INSERT INTO \"Messwerte\" (\""+REL_CONDITION+"\", \""+ATT_TIME+"\", \""+ATT_TIMEUNIT+"\", \""+ATT_LOG10N+"\", \""+ATT_LOG10NUNIT+"\" )VALUES( ?, ?, 'Stunde', ?, '1' )" );
			ps.setInt( 1, condId );
			if (timeId >= 0) {
				ps.setInt(2, timeId);
			}
			else {
				ps.setNull(2, Types.INTEGER);
			}
			if (lognId >= 0) {
				ps.setInt(3, lognId);
			}
			else {
				ps.setNull(3, Types.INTEGER);
			}
			
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
	}
	
	private int insertParam(final int modelId, final String paramName, final int paramType, final Double min, final Double max) {
		
		PreparedStatement ps;
				
		int id = -1;
		try {
			int paramId = queryParamId(modelId, paramName, paramType);
			if( paramId <= 0 ) {
				ps = conn.prepareStatement( "INSERT INTO \"ModellkatalogParameter\" ( \"Modell\", \"Parametername\", \"Parametertyp\", \"min\",\"max\" ) VALUES( ?, ?, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS );				
			}
			else {
				ps = conn.prepareStatement( "UPDATE \"ModellkatalogParameter\" SET \"Modell\" = ?, \"Parametername\" = ?, \"Parametertyp\" = ?, \"min\"= ?, \"max\" = ? WHERE \"ID\"=" + paramId, Statement.RETURN_GENERATED_KEYS );								
			}
				
			ps.setInt( 1, modelId );
			ps.setString( 2, paramName );
			ps.setInt( 3, paramType );
			if (min == null || paramType != PARAMTYPE_PARAM) {
				ps.setNull(4, Types.DOUBLE);
			} else {
				ps.setDouble(4, min);
			}
			if (max == null || paramType != PARAMTYPE_PARAM) {
				ps.setNull(5, Types.DOUBLE);
			} else {
				ps.setDouble(5, max);
			}
			
			if( ps.executeUpdate() < 1 ) {
				return id;
			}
			if (paramId > 0) {
				return paramId;
			}
			
			ResultSet result = ps.getGeneratedKeys();
			
			if( !result.next() ) {
				return id;
			}

			id = result.getInt(1);
			
			result.close();
			ps.close();

		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		
		return id;
	}
	
	private Integer getId4Formula(final String formula) {
		Integer o = (Integer) DBKernel.getValue(super.getConnection(), "Modellkatalog", "Formel", formula, "ID");
		return o;
	}
	private boolean isObjectPresent( final String tablename, final int id ) {
		
		if( id <= 0 ) {
			return false;
		}
		
		int cnt = 0;
		try {
			PreparedStatement ps = conn.prepareStatement( "SELECT COUNT( * )FROM \""+tablename+"\" WHERE \"ID\"=?" );
			ps.setInt( 1, id );
			
			ResultSet result = ps.executeQuery();
			
			result.next();
				
			
			cnt = result.getInt( 1 );
						
			result.close();
			ps.close();
		}
		catch( SQLException ex ) { ex.printStackTrace(); }
		
		if( cnt > 0 ) {
			return true;
		}
		
		return false;
	}
	
	private void insertEstParam(final int estModelId, final int paramId, final Double value, final Double paramErr) {
		PreparedStatement ps = null;
		try {
			
			ps = conn.prepareStatement( "INSERT INTO \"GeschaetzteParameter\" ( \"GeschaetztesModell\", \"Parameter\", \"Wert\", \"StandardError\" ) VALUES( ?, ?, ?, ? )" );
			ps.setInt(1, estModelId);
			ps.setInt(2, paramId);
			if (value == null || Double.isNaN(value)) {
				ps.setNull(3, Types.DOUBLE);
			} else {
				ps.setDouble(3, value);
			}
			if (paramErr == null || Double.isNaN(paramErr)) {
				ps.setNull(4, Types.DOUBLE);
			} else {
				ps.setDouble(4, paramErr);
			}
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException ex) {
			System.out.println(ps);
			ex.printStackTrace();
		}
	}
	
	private int queryParamId( final int modelId, final String paramName, final int paramType ) {
		int ret = -1;
		try {			
			PreparedStatement ps = conn.prepareStatement( "SELECT \"ID\" FROM \""+REL_PARAM+"\"  WHERE \""+ATT_MODELID+"\"=? AND \""+ATT_PARAMNAME+"\" LIKE ? AND \""+ATT_PARAMTYPE+"\"=?" );
			ps.setInt( 1, modelId );
			ps.setString( 2, paramName );
			ps.setInt( 3, paramType );
			
			//System.out.println( ps );
			
			ResultSet result = ps.executeQuery();
			if( !result.next() ) {
				return ret;
			}
			ret = result.getInt( 1 );
			
			result.close();
			ps.close();
		}
		catch( SQLException ex ) {ex.printStackTrace();}
		
		return ret;
	}
	
	private void updateEstModel( final int estModelId, final int condId, final int modelId,
		final double rms, final double rsquared, final double aic, final double bic, final int responseId ) {
		try {
			
			PreparedStatement ps = conn.prepareStatement("UPDATE \"GeschaetzteModelle\" SET \"Versuchsbedingung\"=?, \"Modell\"=?, \"RMS\"=?, \"Rsquared\"=?, \"AIC\"=?, \"BIC\"=?, \"Response\"=? WHERE \"ID\"=?");
			if (condId > 0) {
				ps.setInt(1, condId);
			} else {
				ps.setNull(1, Types.INTEGER);
			}
			ps.setInt(2, modelId);
			if(Double.isNaN(rms)) {
				ps.setNull(3, Types.DOUBLE);
			} else {
				ps.setDouble(3, rms);
			}
			if( Double.isNaN( rsquared ) ) {
				ps.setNull(4, Types.DOUBLE);
			} else {
				ps.setDouble(4, rsquared);
			}
			if (Double.isNaN(aic)) {
				ps.setNull(5, Types.DOUBLE);
			} else {
				ps.setDouble(5, aic);
			}
			if (Double.isNaN(bic)) {
				ps.setNull(6, Types.DOUBLE);
			} else {
				ps.setDouble(6, bic);
			}
			if( responseId > 0 ) {
				ps.setInt(7, responseId);
			} else {
				ps.setNull(7, Types.INTEGER);
			}
			ps.setInt(8, estModelId);
			
			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException ex) {ex.printStackTrace();}
	}
	
	private int insertEstModel(final int condId, final int modelId, final Double rms,
		final Double rsquared, final Double aic, final Double bic, final int responseId) {		
		int ret = -1;
		try {
			
			PreparedStatement ps = conn.prepareStatement("INSERT INTO \"GeschaetzteModelle\" (\"Versuchsbedingung\", \"Modell\", \"RMS\", \"Rsquared\", \"AIC\", \"BIC\", \"Response\" ) VALUES( ?, ?, ?, ?, ?, ?, ? )", Statement.RETURN_GENERATED_KEYS);
			if( condId > 0 ) {
				ps.setInt( 1, condId );
			} else {
				ps.setNull( 1, Types.INTEGER );
			}
			ps.setInt( 2, modelId );
			if (rms == null || Double.isNaN(rms)) {
				ps.setNull( 3, Types.DOUBLE );
			} else {
				ps.setDouble(3, rms);
			}
			if (rsquared == null || Double.isNaN(rsquared)) {
				ps.setNull(4, Types.DOUBLE);
			} else {
				ps.setDouble(4, rsquared);
			}
			if (aic == null || Double.isNaN(aic)) {
				ps.setNull(5, Types.DOUBLE);
			} else {
				ps.setDouble(5, aic);
			}
			if (bic == null || Double.isNaN(bic)) {
				ps.setNull(6, Types.DOUBLE);
			} else {
				ps.setDouble(6, bic);
			}
			if (responseId > 0) {
				ps.setInt(7, responseId);
			} else {
				ps.setNull(7, Types.INTEGER);
			}

			ps.executeUpdate();
			ResultSet result = ps.getGeneratedKeys();
			result.next();			
			ret = result.getInt( 1 );
			
			result.close();
			ps.close();
			
		}
		catch( SQLException ex ) {
			ex.printStackTrace();
		}
		
		return ret;
	}
		
	private void deleteParamNotIn( final int modelId, final LinkedList<Integer> paramIdSet ) {
		
		PreparedStatement ps;
		String q, r;
		
		
		r = "( ";
		for( Integer i : paramIdSet ) {
			
			if( !r.equals( "( " ) ) {
				r +=", ";
			}
			
			r += i;
		}
		r += ")";
		
		q = "DELETE FROM \""+REL_PARAM+"\" WHERE \"Modell\"=" + modelId + " AND \"ID\" NOT IN " + r;
		
		try {
			
			ps = conn.prepareStatement( q );
			ps.executeUpdate();
			ps.close();
		}
		catch( SQLException ex ) {
			// ex.printStackTrace();
		}
	}
	
    private PmmXmlDoc getLiterature( String s ) {
		PmmXmlDoc l = new PmmXmlDoc();
		String [] ids = s.split(",");
		for (String id : ids) {
			Object author = DBKernel.getValue(conn,"Literatur", "ID", id, "Erstautor");
			Object year = DBKernel.getValue(conn,"Literatur", "ID", id, "Jahr");
			Object title = DBKernel.getValue(conn,"Literatur", "ID", id, "Titel");
			Object abstrac = DBKernel.getValue(conn,"Literatur", "ID", id, "Abstract");
			LiteratureItem li = new LiteratureItem(author == null ? null : author.toString(),
					(Integer) (year == null ? null : year),
					title == null ? null : title.toString(),
					abstrac == null ? null : abstrac.toString(),
					Integer.valueOf(id)); 
			l.add(li);
		}    
		return l;
    }

}
