package tinyos.yeti.nesc12.ep.rules.proposals;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.ProposalUtility;
import tinyos.yeti.nesc12.parser.CompletionProposalCollector;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;

public class AccessDataObjectRuleByField extends AccessRuleByField{
	public AccessDataObjectRuleByField(){
		super( "." );
	}

	@Override
	protected void propose( Field beforeAccessSign, NesC12AST ast, CompletionProposalCollector collector ){
		Type type = beforeAccessSign.getType();
		if( type == null )
			return;
		
        DataObjectType data = type.asDataObjectType();
        if( data == null )
            return;
        
        for( Field field : data.getAllFields() ){
            collector.add( ProposalUtility.createProposal( field, collector.getLocation(), ast ) );
        }
	}
}
