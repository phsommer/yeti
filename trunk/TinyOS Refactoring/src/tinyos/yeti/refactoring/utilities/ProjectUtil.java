package tinyos.yeti.refactoring.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.builder.ProjectResourceCollector;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.refactoring.RefactoringPlugin;
import tinyos.yeti.refactoring.RefactoringPlugin.LogLevel;

public class ProjectUtil {
	
	private NesCEditor editor;
	private IProject project; 
	
	public ProjectUtil(NesCEditor editor){
		this.editor=editor;
		this.project=editor.getProject();
		
	}
	
	/**
	 * Parses a given File and returns the Parser.
	 */
	public Parser getParser(IFile iFile, IProgressMonitor monitor) throws IOException, MissingNatureException{
		ProjectModel projectModel = TinyOSPlugin.getDefault().getProjectTOS(project).getModel();

		File file = iFile.getLocation().toFile();
		IParseFile parseFile = projectModel.parseFile(file);

		Parser parser = (Parser) projectModel.newParser(parseFile, null, monitor);
		parser.setCreateAST(true);
		parser.setFollowIncludes(true);
		parser.setGatherGlobalFieldInformation(true);
		parser.setResolveFullModel(true);
		parser.setASTModel(editor.getASTModel());
		parser.parse(new FileMultiReader(file), monitor);
		return parser;
	}

	/**
	 * Returns all files of this project.
	 * @return
	 * @throws CoreException
	 */
	public Collection<IFile> getAllFiles() throws CoreException{
		ProjectResourceCollector collector = new ProjectResourceCollector();
		try {
			TinyOSPlugin.getDefault().getProjectTOS(project).acceptSourceFiles(collector);
		} catch (MissingNatureException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					"Refactroing was called while Plugin was not ready: "
							+ e.getMessage());
			throw new CoreException(new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Plugin wasn't ready while calling Rename global Variable Refactoring: "
							+ e.getMessage()));
		}
		Collection<IFile> files=new LinkedList<IFile>();
		for(IResource resource:collector.resources){
			if(resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				files.add(file);
			}
		}
		return files;
	}
	
	/**
	 * Returns the projectModel of this project.
	 * @return
	 * @throws MissingNatureException
	 */
	public ProjectModel getModel() throws MissingNatureException{
		return TinyOSPlugin.getDefault().getProjectTOS(editor.getProject()).getModel();
	}
}
