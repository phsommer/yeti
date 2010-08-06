package tinyos.yeti.refactoring.rename.global;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import tinyos.yeti.ep.parser.IASTModelPath;

/**
 * A class which encapsulates data about a specific global field.
 * @author Max Urech
 *
 */
public  class FieldInfoSet{
	private String fieldIdentifier;
	private Map<IFile, Collection<FieldInfo>> files2FieldInfos;
	

	public FieldInfoSet(String fieldIdentifier,Map<IFile, Collection<FieldInfo>> files2FieldInfos) {
		this.fieldIdentifier=fieldIdentifier;
		this.files2FieldInfos=files2FieldInfos;
	}

	/**
	 * Returns the name of the field to which this set contains information.
	 * @return
	 */
	public String getFieldIdentifier() {
		return fieldIdentifier;
	}

	/**
	 * Returns a map which maps to each file the field infos, for the fields which are occurrences of this field name.
	 * @return
	 */
	public Map<IFile, Collection<FieldInfo>> getFiles2FieldInfos() {
		return files2FieldInfos;
	}
	
	/**
	 * Returns all fieldInfos for the fields which are occurrences of this field name, independent of the file in which they occur.
	 * @return
	 */
	public Collection<FieldInfo> getAllFieldInfos(){
		Collection<FieldInfo> resultInfos=new LinkedList<FieldInfo>();
		for(IFile file:files2FieldInfos.keySet()){
			Collection<FieldInfo> infosOfFile=files2FieldInfos.get(file);
			resultInfos.addAll(infosOfFile);
		}
		return resultInfos;
	}
	
	/**
	 * Returns all paths which are associated with a field of this set.
	 * @return
	 */
	public Collection<IASTModelPath> getAllKnownPaths(){
		Collection<IASTModelPath> resultPaths=new LinkedList<IASTModelPath>();
		for(FieldInfo fieldInfo:getAllFieldInfos()){
			IASTModelPath path=fieldInfo.getField().getPath();
			if(path!=null){
				resultPaths.add(path);
			}
		}
		return resultPaths;
	}
	
	/**
	 * Returns all paths which are associated with a field of this set and the given file.
	 * @return
	 */
	public Collection<IASTModelPath> getKnownPathsForFile(IFile file){
		Collection<IASTModelPath> resultPaths=new LinkedList<IASTModelPath>();
		for(FieldInfo fieldInfo:files2FieldInfos.get(file)){
			IASTModelPath path=fieldInfo.getField().getPath();
			if(path!=null){
				resultPaths.add(path);
			}
		}
		return resultPaths;
	}
	
	/**
	 * Checks if there are any fields in this set.
	 * @return
	 */
	public boolean isEmpty(){
		return files2FieldInfos.size()==0;
	}
	
}
