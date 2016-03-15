import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.pmf.PMFConstants;
import org.sbml.jsbml.ext.pmf.PMFModelPlugin;
import org.sbml.jsbml.ext.pmf.PMFParameterPlugin;
import org.sbml.jsbml.ext.pmf.PMFReference;
import org.sbml.jsbml.ext.pmf.PMFRulePlugin;
import org.sbml.jsbml.ext.pmf.PMFUnitDefinitionPlugin;
import org.sbml.jsbml.ext.pmf.PmmLabId;
import org.sbml.jsbml.text.parser.ParseException;

public class Test {
  
  public static void main(String[] args) {
    SBMLDocument doc = new SBMLDocument(3, 1);
    doc.enablePackage(PMFConstants.shortLabel);
    Model model = doc.createModel();
    
    PMFModelPlugin plugin = (PMFModelPlugin) model.createPlugin(PMFConstants.shortLabel);
    plugin.createModelVariable("pH", 5.0);
    plugin.createModelVariable("T", 20.0);
    
    Parameter p = model.createParameter("p");
    PMFParameterPlugin parameterPlugin = (PMFParameterPlugin) p.createPlugin(PMFConstants.shortLabel);
    parameterPlugin.createP(0.1);
    parameterPlugin.createT(0.3);
    parameterPlugin.createError(0.5);
    parameterPlugin.createDescription("some description");
    parameterPlugin.createMin(0.1);
    parameterPlugin.createMax(9.9);
    parameterPlugin.createCorrelation("a", 1.0);
    parameterPlugin.createCorrelation("b", 2.0);
    
    UnitDefinition ud = model.createUnitDefinition("ln_count_g");
    ud.setName("ln(count/g)");
    PMFUnitDefinitionPlugin unitDefinitionPlugin = (PMFUnitDefinitionPlugin) ud.createPlugin(PMFConstants.shortLabel);
    unitDefinitionPlugin.createUnitTransformation("ln");
    
    Rule rule = model.createAlgebraicRule();
    try {
    rule.setMath(ASTNode.parseFormula("2+2"));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    PMFRulePlugin rulePlugin = (PMFRulePlugin) rule.createPlugin(PMFConstants.shortLabel);
    rulePlugin.createFormulaName("2 plus 2");
    PMFReference ref = new PMFReference();
    ref.setTitle("Baranyi latest model");
    rulePlugin.addReference(ref);
    rulePlugin.setPmmLabId(new PmmLabId(1));
    
    try {
      System.out.println(JSBML.writeSBMLToString(doc));
      JSBML.writeSBML(doc, "C:/Temp/theFile.sbml");
        doc = JSBML.readSBMLFromFile("C:/Temp/theFile.sbml");
        
        System.out.println();
        System.out.println(JSBML.writeSBMLToString(doc));
      } catch (XMLStreamException | IOException e) {
      e.printStackTrace();
    }
  }
}
