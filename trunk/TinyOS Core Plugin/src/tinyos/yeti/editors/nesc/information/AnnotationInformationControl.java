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
package tinyos.yeti.editors.nesc.information;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

/**
 * Information control showing an {@link Annotation}.
 * @author Benjamin Sigg
 */
public class AnnotationInformationControl extends AbstractInformationControl{
	public static class Factory implements INesCInformationControlFactory<Annotation>{
		public INesCInformationControl create( Composite parent, Annotation input, INesCInformationControlOwner owner ){
			return new AnnotationInformationControl( parent, input, owner );
		}
	};
	
	private DefaultMarkerAnnotationAccess markerAnnotationAccess= new DefaultMarkerAnnotationAccess();
	
	public AnnotationInformationControl( Composite parent, Annotation annotation, INesCInformationControlOwner owner ){
		super( owner );
		createAnnotationInformation( parent, annotation );
		setColorAndFont( parent );
	}
	
	/**
	 * Default constructor doing nothing
	 * @param owner owner of this control
	 */
	protected AnnotationInformationControl( INesCInformationControlOwner owner ){
		super( owner );
	}
	
	public void dispose(){
		// ignore	
	}
	
	protected void createAnnotationInformation( Composite parent, final Annotation annotation ){
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 2;
		layout.marginWidth= 2;
		layout.horizontalSpacing= 0;
		composite.setLayout(layout);

		final Canvas canvas= new Canvas(composite, SWT.NO_FOCUS);
		GridData gridData= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gridData.widthHint= 17;
		gridData.heightHint= 16;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.setFont(null);
				markerAnnotationAccess.paint(annotation, e.gc, canvas, new Rectangle(0, 0, 16, 16));
			}
		});

		StyledText text= new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
		text.setLayoutData(data);
		text.setText(annotation.getText());
	}
}
