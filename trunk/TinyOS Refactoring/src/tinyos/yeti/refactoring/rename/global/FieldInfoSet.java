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
	Map<IFile, Collection<FieldInfo>> files2FieldInfos;
	

	public FieldInfoSet(String fieldIdentifier,Map<IFile, Collection<FieldInfo>> files2FieldInfos) {
		this.fieldIdentifier=fieldIdentifier;
		this.files2FieldInfos=files2FieldInfos;
	}

	public String getFieldIdentifier() {
		return fieldIdentifier;
	}

	public Map<IFile, Collection<FieldInfo>> getFiles2FieldInfos() {
		return files2FieldInfos;
	}
	
	public Collection<FieldInfo> getAllFieldInfos(){
		Collection<FieldInfo> resultInfos=new LinkedList<FieldInfo>();
		for(IFile file:files2FieldInfos.keySet()){
			Collection<FieldInfo> infosOfFile=files2FieldInfos.get(file);
			resultInfos.addAll(infosOfFile);
		}
		return resultInfos;
	}
	
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
	
}
