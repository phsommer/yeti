package tinyos.yeti.refactoring.entities.field.rename.global;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.refactoring.utilities.DebugUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;


/**
 * GlobalFieldFinder can gather information about the fields in the global scope of a project.
 * Information are about Declarations and Definitions and References.
 * @author Max Urech
 *
 */
public class GlobalFieldFinder {
	
	private IProgressMonitor monitor;
	private NesCEditor editor;

	
	public GlobalFieldFinder(NesCEditor editor,IProgressMonitor monitor) 
	throws CoreException, IOException, MissingNatureException{
		this.editor=editor;
		this.monitor=monitor;
		collectFieldInformation();
	}
	
	/**
	 * Contains all information about the fields in the project.
	 * Maps fileNames to Maps which map identifier names to fields which have the given name in the given file.
	 */
	private Map<IFile,Map<String, Collection<Field>>> files2FieldNameMap=new HashMap<IFile, Map<String,Collection<Field>>>();
	
	
	/**
	 * Collects the information about the field which are later on kept in the map files2FieldNameMap.
	 * @throws CoreException
	 * @throws IOException
	 * @throws MissingNatureException
	 */
	private void collectFieldInformation() 
	throws CoreException, IOException, MissingNatureException{
		ProjectUtil projectUtil=new ProjectUtil(editor);
		for (IFile file : projectUtil.getAllFiles()) {
			Parser parser = projectUtil.getParser(file, monitor);
			Map<String, Collection<Field>> fieldsToAssociatedPaths=parser.getFieldsToAssociatedPaths();
			files2FieldNameMap.put(file, fieldsToAssociatedPaths);
		}
	}
	
	/**
	 * Returns all fields in the project for the given field identifier mapped by the file in which they appear.
	 * @param fieldIdentifier the field name which we are interested in.
	 * @return Map which maps files to the fields which have the given fieldIdentifier.
	 */
	private Map<IFile, Collection<Field>> getFieldInformationAbout(String fieldIdentifier){
		Map<IFile, Collection<Field>> file2Fields=new HashMap<IFile,Collection<Field>>();
		for(IFile file:files2FieldNameMap.keySet()){
			Map<String, Collection<Field>> fieldName2Fields=files2FieldNameMap.get(file);
			Collection<Field> fields=fieldName2Fields.get(fieldIdentifier);
			if(fields!=null&&fields.size()>0){
				file2Fields.put(file, fields);
			}
		}
		return file2Fields;
	}
	
	/**
	 * Creates a FieldInfoSet for the given fieldIdentifier.
	 * The FieldInfoSet contains information bout a globalField.
	 * @param fieldIdentifier
	 * @return
	 */
	public FieldInfoSet getFieldInfoSetAbout(String fieldIdentifier){
		Map<IFile,Collection<FieldInfo>> files2FieldInfos=new HashMap<IFile, Collection<FieldInfo>>();
		Map<IFile,Collection<Field>> file2Fields=getFieldInformationAbout(fieldIdentifier);
		for(IFile file:file2Fields.keySet()){
			Collection<Field> fields=file2Fields.get(file);
			for(Field field:fields){
				FieldKind kind=decideFieldKind(field);
				Collection<FieldInfo> fieldInfos=files2FieldInfos.get(file);
				if(fieldInfos==null){
					fieldInfos=new LinkedList<FieldInfo>();
					files2FieldInfos.put(file, fieldInfos);
				}
				fieldInfos.add(new FieldInfo(file,field,kind));
			}
		}
		return new FieldInfoSet(fieldIdentifier, files2FieldInfos);
	}
	
	/**
	 * Tries to decide what kind of field this field is.
	 * I.e. if it is a declaration, an included_declaration or a definition.
	 * @param field
	 * @return
	 */
	private FieldKind decideFieldKind(Field field) {
		FieldModelNode node=field.asNode();
		if(node==null){
			return FieldKind.FORWARD_DECLARATION;
		}
		TagSet tags=node.getTags();
		if(tags.contains(Tag.INCLUDED)){
			return FieldKind.INCLUDED_DECLARATION;
		}
		//TODO the Decision made here is not correct for definitions.
		return FieldKind.DECLARATION;
	}
	
	/**
	 * Prints all known data about the given field.
	 * Only useful for debugging purposes.
	 * @param fieldIdentifier 
	 * @throws CoreException
	 */
	public void printFieldInformation(String fieldIdentifier) throws CoreException{
		ProjectUtil projectUtil=new ProjectUtil(editor);
		try {
			for (IFile file : projectUtil.getAllFiles()) {
				Parser parser = projectUtil.getParser(file, monitor);
				Map<String, Collection<Field>> fields2AssociatedPaths=parser.getFieldsToAssociatedPaths();
				if(fields2AssociatedPaths==null){
					DebugUtil.addOutput("fields is null");
				}
				else{
					Collection<Field> paths=fields2AssociatedPaths.get(fieldIdentifier);
					DebugUtil.addOutput("file: "+file.getName().toString());
					if(paths==null){
						DebugUtil.addOutput("\tcontains no such name");
					}else{
						for(Field field:paths){
							if(field!=null){
								if(field.asNode()!=null){
									DebugUtil.addOutput("\t"+field.asNode().toString());
								}else{
									DebugUtil.addOutput("\tnode was null");
								}
								if(field.getPath()!=null){
									DebugUtil.addOutput("\t"+field.getPath().toString());
								}else{
									DebugUtil.addOutput("\tpath was null");
								}
								if(field.getRange()!=null){
									DebugUtil.addOutput("\t"+field.getRange().getLeft()+"<->"+field.getRange().getRight());
								}else{
									DebugUtil.addOutput("\trange was null");
								}
							}else{
								DebugUtil.addOutput("\tfield was null");
							}
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingNatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DebugUtil.printOutput();
	}
}
