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
package tinyos.yeti.widgets;

/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.WorkbenchJob;

import tinyos.yeti.widgets.helper.PatternFilter;

/**
 * A simple control that provides a text widget and a tree viewer. The contents
 * of the text widget are used to drive a PatternFilter that is on the viewer.
 * 
 * @since 3.0
 */
public class FilteredTree extends Composite {    
    private IFilterViewer filterTextViewer;
    
    private TreeViewer treeViewer;

    private ITreeFilter filter;
    private ViewerFilter currentFilter;

    private AccessibleAdapter accessibleListener;
    
    //The job for refreshing the tree
    private Job refreshJob;

    /**
     * Create a new instance of the receiver. It will be created with a default
     * pattern filter.
     * 
     * @param parent the parent composite
     * @param treeStyle the SWT style bits to be passed to the tree viewer
     * @param filterViewer the viewer for the filter
     */
    public FilteredTree(Composite parent, int treeStyle, IFilterViewer filterViewer) {
        this(parent, treeStyle, new PatternFilter(), filterViewer);
    }

    /**
     * Create a new instance of the receiver.
     * 
     * @param parent parent <code>Composite</code>
     * @param treeStyle the style bits for the <code>Tree</code>
     * @param filter the filter to be used
     * @param filterViewer the viewer that shows the selected filter
     */
    public FilteredTree(Composite parent, int treeStyle, ITreeFilter filter, IFilterViewer filterViewer) {
        super(parent, SWT.NONE);
        this.filter = filter;
        GridLayout layout = new GridLayout( 1, false );
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        treeViewer = new TreeViewer(this, treeStyle);
        treeViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        treeViewer.getControl().addDisposeListener(new DisposeListener(){
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
                refreshJob.cancel();
            }
        });
        
        currentFilter = filter.filter( "" );
        treeViewer.addFilter( currentFilter );

        createRefreshJob();
        
        this.filterTextViewer = filterViewer;
        filterViewer.install( this );
    }

    /**
     * Create the refresh job for the receiver.
     *
     */
    private void createRefreshJob() {
        refreshJob = new WorkbenchJob("Refresh Filter"){//$NON-NLS-1$
            /* (non-Javadoc)
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if(treeViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;

                refreshRun();
                
                return Status.OK_STATUS;
            }

        };
        refreshJob.setSystem(true);
    }
    
    public AccessibleAdapter getAccessibleListener() {
        if( accessibleListener == null )
            accessibleListener = createAccessibleListener();
        
        return accessibleListener;
    }
    
    private AccessibleAdapter createAccessibleListener(){
        return new AccessibleAdapter(){
            /* (non-Javadoc)
             * @see org.eclipse.swt.accessibility.AccessibleListener#getName(org.eclipse.swt.accessibility.AccessibleEvent)
             */
            @Override
            public void getName(AccessibleEvent e) {
                String filterTextString = getFilterText();
                if(filterTextString.length() == 0){
                    e.result = getInitialText();
                }
                else
                    e.result = filterTextString;
            }
        };
    }

    /**
     * Get the text from the filter widget.
     * @return String
     */
    protected String getFilterText() {
        return filterTextViewer.getFilterText();
    }
    
    protected String getInitialText(){
        return filterTextViewer.getInitialText();
    }
    
    /**
     * update the receiver after the text has changed
     * @param immediate if set, then the update is performed as fast as possible,
     * otherwise a delay is used to cummulate updates
     */
    public void refreshFilter( boolean immediate ){
        if( immediate ){
            refreshJob.cancel();
            refreshRun();
        }
        else{
            refreshJob.schedule(1500);
        }
    }
    
    private void refreshRun(){
        String filterText = getFilterText();
        boolean initial = filterText.equals( filterTextViewer.getInitialText() );
        ViewerFilter viewerFilter = null;
        if( initial ){
            viewerFilter = filter.filter( "" );
        }
        else {
            viewerFilter = filter.filter( getFilterText() );
        }
        
        if( currentFilter == viewerFilter ){
            treeViewer.refresh();
        }
        else{
            ViewerFilter[] filters = treeViewer.getFilters();
            ViewerFilter[] replacement = new ViewerFilter[ filters.length ];
            for( int i = 0, n = filters.length; i<n; i++ ){
                if( filters[i] == currentFilter )
                    replacement[i] = viewerFilter;
                else
                    replacement[i] = filters[i];
            }
            currentFilter = viewerFilter;
            treeViewer.setFilters( replacement );
        }

    }
    
    /**
     * Get the tree viewer associated with this control.
     * 
     * @return the tree viewer
     */
    public TreeViewer getViewer() {
        return treeViewer;
    }

}
