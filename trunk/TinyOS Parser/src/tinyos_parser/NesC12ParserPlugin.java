/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
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
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos_parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import tinyos.yeti.nesc12.ep.rules.hover.IHoverInformationRule;
import tinyos.yeti.nesc12.ep.rules.hyperlink.IHyperlinkRule;
import tinyos.yeti.nesc12.ep.rules.proposals.IProposalRule;
import tinyos.yeti.nesc12.ep.rules.quickfix.IMultiQuickfixRule;
import tinyos.yeti.nesc12.ep.rules.quickfix.ISingleQuickfixRule;

/**
 * The activator class controls the plug-in life cycle
 */
public class NesC12ParserPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "tinyos.yeti.parser.nesc12";

    // The shared instance
    private static NesC12ParserPlugin plugin;

    private IProposalRule[] proposals;
    private IHoverInformationRule[] hovers;
    private IHyperlinkRule[] hyperlinks;
    
    private ISingleQuickfixRule[] singleQuickfixes;
    private IMultiQuickfixRule[] multiQuickfixes;
    
    /**
     * The constructor
     */
    public NesC12ParserPlugin() {
        plugin = this;
    }

    public ISingleQuickfixRule[] getSingleQuickfixRules(){
        ensureSingleQuickfixes();
        return singleQuickfixes;
    }
    
    private void ensureSingleQuickfixes(){
        if( singleQuickfixes == null ){
            List<ISingleQuickfixRule> rules = loadExtensionPoints( "nesc12.parser.quickfixes", "single", "class" );
            singleQuickfixes = rules.toArray( new ISingleQuickfixRule[ rules.size() ] );
        }
    }

    public IMultiQuickfixRule[] getMultiQuickfixRules(){
        ensureMultiQuickfixes();
        return multiQuickfixes;
    }
    
    private void ensureMultiQuickfixes(){
        if( multiQuickfixes == null ){
            List<IMultiQuickfixRule> rules = loadExtensionPoints( "nesc12.parser.quickfixes", "multi", "class" );
            multiQuickfixes = rules.toArray( new IMultiQuickfixRule[ rules.size() ] );
        }
    }
    
    public IHyperlinkRule[] getHyperlinkRules(){
        ensureHyperlinkRules();
        return hyperlinks;
    }
    
    private void ensureHyperlinkRules(){
        if( hyperlinks == null ){
            List<IHyperlinkRule> rules = loadExtensionPoints( "nesc12.parser.hyperlinks", "rule", "class" );
            hyperlinks = rules.toArray( new IHyperlinkRule[ rules.size() ] );
        }
    }
    
    public IHoverInformationRule[] getHoverRules(){
    	ensureHoverRules();
    	return hovers;
    }
    
    private void ensureHoverRules(){
    	if( hovers == null ){
    		List<IHoverInformationRule> rules = loadExtensionPoints( "nesc12.parser.hover", "rule", "class" );
    		hovers = rules.toArray( new IHoverInformationRule[ rules.size() ] );
    	}
    }
    
    public IProposalRule[] getProposalRules(){
        ensureProposalRules();
        return proposals;
    }

    private void ensureProposalRules(){
        if( proposals == null ){
            List<IProposalRule> rules = loadExtensionPoints( "nesc12.parser.proposals", "proposal", "class" );
            proposals = rules.toArray( new IProposalRule[ rules.size() ]);
        }
    }


    @SuppressWarnings( "unchecked" )
    private <E> List<E> loadExtensionPoints( String point, String extension, String executableAttribute ){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( point );
        List<E> result = new ArrayList<E>();

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements()){
                if( element.getName().equals( extension )){
                    try{
                        result.add( (E)element.createExecutableExtension( executableAttribute ) );
                    }
                    catch( CoreException e ){
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static NesC12ParserPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
