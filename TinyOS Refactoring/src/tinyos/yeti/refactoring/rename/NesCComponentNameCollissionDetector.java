package tinyos.yeti.refactoring.rename;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class NesCComponentNameCollissionDetector {
	
	private ASTUtil astUtil=new ASTUtil();
	
	/**
	 * Checks if the newFileName already exists in the project.
	 * If it already exists, an error is appended to ret with context information.
	 * @param newFileName
	 * @param entityIdentifier	The entity identifier of the nesc entity which is to be renamed. 
	 * @param projectUtil
	 * @param ret
	 */
	public void handleCollisions4NewFileName(String newFileName, Identifier entityIdentifier, IFile toRename,ProjectUtil projectUtil,RefactoringStatus ret, IProgressMonitor pm){
		try {
			Collection<IFile> files=projectUtil.getAllFiles();
			for(IFile file:files){
				if(newFileName.equals(file.getName())){
					Region toRenameRegion= new Region(entityIdentifier.getRange().getLeft(),entityIdentifier.getName().length());
					ret.addError("You intended to rename the file "+toRename.getName()+" to "+file.getName(),new FileStatusContext(toRename, toRenameRegion));
					AstAnalyzerFactory factory=new AstAnalyzerFactory(file,projectUtil,pm);
					if(factory.hasNesCAnalyzerCreated()){
						Identifier alreadyExisting=factory.getNesCAnalyzer().getEntityIdentifier();
						Region sameNameRegion= new Region(alreadyExisting.getRange().getLeft(),alreadyExisting.getName().length());
						ret.addError("There is already a file in the project with the name: "+newFileName,new FileStatusContext(file, sameNameRegion));
					}else{
						ret.addError("There is already a file in the project with the name: "+newFileName);
					}
				}
			}
		} catch (Exception e) {
			ret.addError("Exception Occured during check if file name already exists. See error log for more information.");
			projectUtil.log("Exception Occured during check if file name already exists.",e);
		}
	}
	

	/**
	 * 	 * Checks if the new name introduced to the given scope leads to fields in sub scopes which will do shadowing to the renamed field.
	 * 	Adds for every Identifier, which has the name newName, an error to the ret with appropriate context information.
	 *  Where every Identifier means the identifiers, which are a successor of the scope node or the scope node itself.
	 *  If there is no such identifier, this method has no effect and returns a still ok RefacactoringStatus.
	 *  We can consider cases, where this errors actually do not lead to a change in meaning of a program but just to shadowing of names.
	 *  Addidtionally an context information error is added to the ret about the declaring identifier.
	 *  
	 * @param oldName
	 * @param newName
	 * @param declaringIdentifier	An identifier which should be renamed by this refactoring, and shall be shown to the user in case of a conflict. If it is null just the conflicting identifiers will be printed.
	 * @param declaringAst	Is only used if the declaringIdentifier is not null. This ast has to include the declaring identifier.
	 * @param scope
	 * @param containingFile
	 * @param containingAst
	 * @param ret
	 */
	public void handleCollisions4Scope(String oldName,String newName,Identifier declaringIdentifier,IFile declaringFile,NesC12AST declaringAst,ASTNode scope,IFile containingFile,NesC12AST containingAst,RefactoringStatus ret){
		//Check if there already exists an identifier with the new name.
		ASTPositioning positioning;
		ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);
		Collection<Identifier> identifiers=astUtil4Variables.getIncludedIdentifiers(scope,newName,null);
		if(identifiers.size()>0){
			//Add Information for collisions to refactoring status
			int offset;
			int length;
			Region region;
			if(declaringIdentifier!=null){
				positioning=new ASTPositioning(declaringAst);
				offset=positioning.start(declaringIdentifier);
//				offset=declaringIdentifier.getRange().getLeft();
				length=oldName.length();
				region= new Region(offset,length);
				ret.addError("You intended to rename the identifier "+oldName+" to "+newName,new FileStatusContext(declaringFile, region));
			}
			length=newName.length();
			positioning=new ASTPositioning(containingAst);
			for(Identifier id:identifiers){
				offset=positioning.start(id);
				region= new Region(offset,length);
				ret.addError("Leads to shadowing with this identifier: "+id.getName(),new FileStatusContext(containingFile, region));
			}
		}
	}
	
	/**
	 * Checks if the new component name already exists in the given configuration as local name for a component or a interface.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 * @param configurationAnalyzer
	 * @param file
	 * @param oldName
	 * @param newName
	 * @param ret
	 */
	public void handleCollisions4NewComponentNameWithConfigurationLocalName(ConfigurationAstAnalyzer configurationAnalyzer, IFile file, String oldName, String newName,RefactoringStatus ret){
		//Check if there is a local component name with the same name.
		Identifier toRename=existsLocalComponentName(configurationAnalyzer, oldName);
		if(toRename==null){
			return;
		}
		handleCollisionsInConfigurationImplementationScope(configurationAnalyzer, file, toRename, newName, ret);
	}
	
	/**
	 * Checks if the new interface name already exists in the given configuration as local name for a component or a interface.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 * @param configurationAnalyzer
	 * @param file
	 * @param oldName
	 * @param newName
	 * @param ret
	 */
	public void handleCollisions4NewInterfaceNameWithConfigurationLocalName(ConfigurationAstAnalyzer configurationAnalyzer, IFile file, String oldName,String newName, RefactoringStatus ret) {
		Identifier toRename=existsLocalInterfaceName(configurationAnalyzer, oldName);
		if(toRename==null){
			return;
		}
		handleCollisionsInConfigurationImplementationScope(configurationAnalyzer, file, toRename, newName, ret);
		
	}
	
	/**
	 * Checks if the newName collides with an existing local interface or component name.
	 * @param configurationAnalyzer
	 * @param file
	 * @param toRename
	 * @param newName
	 * @param ret
	 */
	private void handleCollisionsInConfigurationImplementationScope(ConfigurationAstAnalyzer configurationAnalyzer, IFile file, Identifier toRename,String newName, RefactoringStatus ret){
		newNameWithLocalComponentName(configurationAnalyzer, file, toRename, newName, ret);
		newNameWithLocalInterfaceName(configurationAnalyzer, file, toRename, newName, ret);
	}
	
	/**
	 * Checks if the Name of the toRename identifier already exists in the given configuration implementation as local name for a component.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 */
	public void newNameWithLocalComponentName(ConfigurationAstAnalyzer configurationAnalyzer, IFile representedFile, String oldName, String newName,RefactoringStatus ret){
		Identifier toRename=existsLocalComponentName(configurationAnalyzer, oldName);
		if(toRename==null){
			return;
		}
		newNameWithLocalComponentName(configurationAnalyzer,representedFile,toRename,newName,ret);
	}
	
	/**
	 * Checks if the Name of the toRename identifier already exists in the given configuration implementation as local name for a component.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 */
	public void newNameWithLocalComponentName(ConfigurationAstAnalyzer configurationAnalyzer, IFile representedFile, Identifier toRename, String newName,RefactoringStatus ret){
		if(toRename==null){
			return;
		}
		Identifier sameName=existsLocalComponentName(configurationAnalyzer,newName);
		if(sameName!=null){
			addInfo(toRename,sameName,representedFile,ret);
		}
	}
	
	/**
	 * Checks if the newName already exists in the given component specification as local name for a interface.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 */
	public void newInterfaceNameWithLocalInterfaceName(ComponentAstAnalyser componentAnalyzer, IFile representedFile, String oldName, String newName,RefactoringStatus ret){
		Identifier toRename=existsLocalInterfaceName(componentAnalyzer, oldName);
		if(toRename==null){
			return;
		}
		newNameWithLocalInterfaceName(componentAnalyzer,representedFile,toRename,newName,ret);
	}
	
	/**
	 * Checks if the Name of the toRename identifier already exists in the given component specification as local name for a interface.
	 * If the newName already exists the RefactoringStatus is marked as erroneous  and context information is added.
	 * If the newName doesnt exist the {@link RefactoringStatus} is not modified.
	 * If the oldName doesnt exist this method doesnt have any effect.
	 */
	public void newNameWithLocalInterfaceName(ComponentAstAnalyser componentAnalyzer, IFile representedFile, Identifier toRename, String newName,RefactoringStatus ret){
		if(toRename==null){
			return;
		}
		Identifier sameName=existsLocalInterfaceName(componentAnalyzer, newName);
		//Add Info
		if(sameName!=null){
			addInfo(toRename,sameName,representedFile,ret);
		}
	}
	
	/**
	 * Returns the component name identifier which has the given name in the configuration implementation scope.
	 * Returns null if there is no component identifier with the given name.
	 * @param analyzer
	 * @param oldName
	 * @return
	 */
	private Identifier existsLocalComponentName(ConfigurationAstAnalyzer analyzer,String name){
		Set<Identifier> localComponentNames=analyzer.getComponentLocalName2ComponentGlobalName().keySet();
		return astUtil.getIdentifierWithEqualName(name, localComponentNames);
	}
	
	/**
	 * Returns the interface name identifier which has the given name in the component specification.
	 * Returns null if there is no component identifier with the given name.
	 * @param analyzer
	 * @param oldName
	 * @return
	 */
	private Identifier existsLocalInterfaceName(ComponentAstAnalyser analyzer,String name){
		Set<Identifier> localInterfaceNames=analyzer.getInterfaceLocalName2InterfaceGlobalName().keySet();
		return astUtil.getIdentifierWithEqualName(name, localInterfaceNames);
	}
	
	/**
	 * Adds error messages and context info to the RefactoringStatus, designating the two colliding definitions in the file.
	 * @param toRename
	 * @param sameName
	 * @param containingFile
	 * @param ret
	 */
	private void addInfo(Identifier toRename,Identifier sameName, IFile containingFile,RefactoringStatus ret){
		Region toRenameRegion= new Region(toRename.getRange().getLeft(),toRename.getName().length());
		Region sameNameRegion= new Region(sameName.getRange().getLeft(),sameName.getName().length());
		ret.addError("You intended to rename the identifier "+toRename.getName()+" to "+sameName.getName(),new FileStatusContext(containingFile, toRenameRegion));
		ret.addError("This would lead to a collision with this identifier: "+sameName.getName(),new FileStatusContext(containingFile, sameNameRegion));
	}

}
