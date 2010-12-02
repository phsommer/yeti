package tinyos.yeti.nesc12.ep.rules.proposals;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.nesc12.ep.INesC12Location;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollection;

/**
 * A rule that analyzes statements like "x . |" (where | is the cursor) and finds
 * out what "x" is. Other than {@link AccessRule} this rule implicitly assumes
 * that the content left of the sign is an identifier.
 * @author Benjamin Sigg
 */
public abstract class AccessRuleByField implements IProposalRule{
    private String accessSign;

    public AccessRuleByField( String accessSign ){
        this.accessSign = accessSign;
    }

    public void propose( NesC12AST ast, CompletionProposalCollector collector ){
        try{
            int accessSignOffset = RuleUtility.begin( collector.getOffset()-1, collector.getLocation().getDocument(), accessSign );
            if( accessSignOffset < 0 )
                return;
    
            INesC12Location location = ast.getOffsetInput( accessSignOffset-1 );
            
            accessSignOffset = RuleUtility.reverseWhitespace( location, collector.getLocation().getDocument() );
            if( accessSignOffset < 0 )
                return;
            
            int identifierOffset = RuleUtility.reverseNonWhitespace( accessSignOffset, collector.getLocation().getDocument() );
            if( identifierOffset < 0 || identifierOffset > accessSignOffset )
            	return;
            
            String id = collector.getLocation().getDocument().get( identifierOffset, accessSignOffset - identifierOffset + 1 );

            GenericRangedCollection ranges = ast.getRanges();
            if( ranges == null )
            	return;
            
            List<Field> fields = ranges.getFields( location.getInputfileOffset() );
            if( fields == null )
            	return;
            
            for( Field field : fields ){
            	Name name = field.getName();
            	if( name != null ){
            		if( id.equals( name.toIdentifier() )){
            			propose( field, ast, collector );
            		}
            	}
            }
        }
        catch( BadLocationException ex ){
            ex.printStackTrace();
        }
    }

    /**
     * Called if the access sign was found before the cursor.
     * @param beforeAccessSign the field before the access sign, not <code>null</code>
     * @param ast the abstract syntax tree
     * @param collector information about what the user is doing
     */
    protected abstract void propose( Field beforeAccessSign, NesC12AST ast, CompletionProposalCollector collector );
}
