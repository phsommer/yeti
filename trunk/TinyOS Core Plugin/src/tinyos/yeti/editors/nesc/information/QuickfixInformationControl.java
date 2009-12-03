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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;

import tinyos.yeti.editors.nesc.IQuickFixInformation;

/**
 * Information control for {@link IQuickFixInformation}.
 * @author Benjamin Sigg
 */
public class QuickfixInformationControl extends AnnotationInformationControl{
	public static class Factory implements INesCInformationControlFactory<IQuickFixInformation>{
		public INesCInformationControl create( Composite parent, IQuickFixInformation input, INesCInformationControlOwner owner ){
			return new QuickfixInformationControl( parent, input, owner );
		}
	}
	
	private IQuickFixInformation input;
	
	public QuickfixInformationControl( Composite parent, IQuickFixInformation input, INesCInformationControlOwner owner ){
		super( parent, input.getAnnotation(), owner );
		
		this.input = input;
		
		ICompletionProposal[] proposals = input.getCompletionProposals();
		if (proposals.length > 0)
			createCompletionProposalsControl( parent, proposals, input );
	}

	private void createCompletionProposalsControl( Composite parent, ICompletionProposal[] proposals, IQuickFixInformation input ){
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout2= new GridLayout(1, false);
		layout2.marginHeight= 0;
		layout2.marginWidth= 0;
		layout2.verticalSpacing= 2;
		composite.setLayout(layout2);

		Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData= new GridData(SWT.FILL, SWT.CENTER, true, false);
		separator.setLayoutData(gridData);

		Label quickFixLabel= new Label(composite, SWT.NONE);
		GridData layoutData= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		layoutData.horizontalIndent= 4;
		quickFixLabel.setLayoutData(layoutData);
		String text;
		if( proposals.length == 1 ){
			text = "There is one quickfix available";
		}
		else {
			text = "There are " + proposals.length + " quickfixes available";
		}
		quickFixLabel.setText(text);

		setColorAndFont(composite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());
		createCompletionProposalsList( composite, proposals, input );
	}

	private void createCompletionProposalsList( Composite parent, ICompletionProposal[] proposals, IQuickFixInformation input ) {
		final ScrolledComposite scrolledComposite= new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
		scrolledComposite.setLayoutData(gridData);
		scrolledComposite.setExpandVertical(false);
		scrolledComposite.setExpandHorizontal(false);

		Composite composite= new Composite(scrolledComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout= new GridLayout(3, false);
		layout.verticalSpacing= 2;
		composite.setLayout(layout);

		final Link[] links= new Link[proposals.length];
		for (int i= 0; i < proposals.length; i++) {
			Label indent= new Label(composite, SWT.NONE);
			GridData gridData1= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gridData1.widthHint= 0;
			indent.setLayoutData(gridData1);

			links[i]= createCompletionProposalLink(composite, proposals[i], input );
		}
		
		owner.setFocus( links[0] );

		scrolledComposite.setContent(composite);
		setColorAndFont(scrolledComposite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());

		Point contentSize= composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		composite.setSize(contentSize);

		Point constraints = owner.getSizeConstraints();
		if (constraints != null && contentSize.x < constraints.x) {
			ScrollBar horizontalBar= scrolledComposite.getHorizontalBar();

			int scrollBarHeight;
			if (horizontalBar == null) {
				Point scrollSize= scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				scrollBarHeight= scrollSize.y - contentSize.y;
			} else {
				scrollBarHeight= horizontalBar.getSize().y;
			}
			gridData.heightHint= contentSize.y - scrollBarHeight;
		}

		for (int i= 0; i < links.length; i++) {
			final int index= i;
			final Link link= links[index];
			link.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
						case SWT.ARROW_DOWN:
							if (index + 1 < links.length) {
								links[index + 1].setFocus();
							}
							break;
						case SWT.ARROW_UP:
							if (index > 0) {
								links[index - 1].setFocus();
							}
							break;
						default:
							break;
					}
				}

				public void keyReleased(KeyEvent e) {
				}
			});

			link.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					int currentPosition= scrolledComposite.getOrigin().y;
					int hight= scrolledComposite.getSize().y;
					int linkPosition= link.getLocation().y;

					if (linkPosition < currentPosition) {
						if (linkPosition < 10)
							linkPosition= 0;

						scrolledComposite.setOrigin(0, linkPosition);
					} else if (linkPosition + 20 > currentPosition + hight) {
						scrolledComposite.setOrigin(0, linkPosition - hight + link.getSize().y);
					}
				}

				public void focusLost(FocusEvent e) {
				}
			});
		}
	}

	private Link createCompletionProposalLink( Composite parent, final ICompletionProposal proposal, final IQuickFixInformation input ){
		Label proposalImage= new Label(parent, SWT.NONE);
		proposalImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		Image image= proposal.getImage();
		if (image != null) {
			proposalImage.setImage(image);

			proposalImage.addMouseListener(new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
				}

				public void mouseDown(MouseEvent e) {
				}

				public void mouseUp(MouseEvent e) {
					if (e.button == 1) {
						apply( proposal, input.getViewer(), input.getOffset() );
					}
				}

			});
		}

		Link proposalLink= new Link(parent, SWT.WRAP);
		proposalLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		proposalLink.setText("<a>" + proposal.getDisplayString() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		proposalLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				apply( proposal, input.getViewer(), input.getOffset() );
			}
		});

		return proposalLink;
	}

	private void apply( ICompletionProposal p, ITextViewer viewer, int offset ){
		//Focus needs to be in the text viewer, otherwise linked mode does not work
		dispose();

		IRewriteTarget target= null;
		try {
			IDocument document= viewer.getDocument();

			if (viewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) viewer;
				target= extension.getRewriteTarget();
			}

			if (target != null)
				target.beginCompoundChange();

			if (p instanceof ICompletionProposalExtension2) {
				ICompletionProposalExtension2 e= (ICompletionProposalExtension2) p;
				e.apply(viewer, (char) 0, SWT.NONE, offset);
			} else if (p instanceof ICompletionProposalExtension) {
				ICompletionProposalExtension e= (ICompletionProposalExtension) p;
				e.apply(document, (char) 0, offset);
			} else {
				p.apply(document);
			}

			Point selection= p.getSelection(document);
			if (selection != null) {
				viewer.setSelectedRange(selection.x, selection.y);
				viewer.revealRange(selection.x, selection.y);
			}
			
			input.proposalApplied();
		}
		finally {
			if (target != null)
				target.endCompoundChange();
		}
	}
}
