package tinyos.yeti.refactoring.utilities;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;

import tinyos.yeti.nesc12.Parser;

class ParserCache {
	private HashMap<IFile, SoftReference<Parser>> parserCache = new HashMap<IFile, SoftReference<Parser>>();	
	private HashMap<IFile, Long> lastModifiedInCache = new HashMap<IFile, Long>();
	
	void add(IFile file, Parser parser){
		parserCache.put(file, new SoftReference<Parser>(parser));
		lastModifiedInCache.put(file, file.getModificationStamp());
	}
	
	/**
	 * Checks if a uptodate version of the file is in the cache
	 * Old versions get removed.
	 */
	boolean contains(IFile file){
		boolean ret = true;
		if(!parserCache.containsKey(file)){
			ret=false;
		}
		
		if(ret &&
				(!lastModifiedInCache.containsKey(file) || // would mean that the cache is inconsistant, so remove
				 !lastModifiedInCache.get(file).equals(file.getModificationStamp()) || // means the cached parser is no longer up to date
				 parserCache.get(file).get() == null) // means the Garbage Collector collected the Parser
		  ){
			ret = false;
			remove(file);
		}
		return ret;
	}

	private void remove(IFile file){
		parserCache.remove(file);
		lastModifiedInCache.remove(file);			
	}
	
	/**
	 * @return if the file is in cache it returns the Parser of the file. Otherwise null.
	 */
	Parser get(IFile file){
		if(contains(file)){
			// if the Garbage collector had removed the parser, it would be null, witch is a valid return value
			Parser parser = parserCache.get(file).get();
			return parser;
		} else {
			return null;
		}
	}
}
