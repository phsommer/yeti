/**
 * 
 */
package tinyos.yeti.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.quickfixer.SingleQuickfixerResolution;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.fix.ISingleQuickFixer;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.marker.ProblemMarkerSupport;
import tinyos.yeti.nature.MissingNatureException;

/**
 * This resolution generator looks out for {@link IMarker}s that were created
 * from {@link IMessage}s (and thus have set the attribute {@link ProblemMarkerSupport#MARKER_MESSAGE_SOURCE}).
 * It then uses the {@link ISingleQuickFixer}s from the {@link TinyOSPlugin#getSingleQuickFixers() TinyOSPlugin}
 * to generate a set of fixes for the given marker.
 * @author Benjamin Sigg
 */
public class NesCMarkerResolutionGenerator implements IMarkerResolutionGenerator {

    public NesCMarkerResolutionGenerator() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        if( marker.getAttribute( ProblemMarkerSupport.MARKER_MESSAGE_SOURCE, false )){
            IResource resource = marker.getResource();
            if (resource instanceof IFile) {
                ISingleQuickFixer[] fixers = TinyOSPlugin.getDefault().getSingleQuickFixers();
                IProject project = resource.getProject();
                ProjectTOS tos = null;
                if( project != null ){
                	try{
                		tos = TinyOSPlugin.getDefault().getProjectTOS( project );
                	}
                	catch( MissingNatureException ex ){
                		// silent;
                	}
                }
                
                if( tos == null )
                    return new IMarkerResolution[0];
                
                IParseFile parseFile = tos.getModel().parseFile( resource );

                List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
                
                for( ISingleQuickFixer fixer : fixers ){
                    ISingleMarkerResolution[] results = fixer.getResolutions( marker, parseFile, tos );
                    if( results != null ){
                        for( ISingleMarkerResolution result : results ){
                            resolutions.add( new SingleQuickfixerResolution( marker, result, resource, tos, parseFile ) );
                        }
                    }
                }
                
                return resolutions.toArray( new IMarkerResolution[ resolutions.size() ] );
            }
        }

        return new IMarkerResolution[0];
    }
}
