package edu.uw.cs.lil.tiny.mr.lambda.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uw.cs.lil.tiny.mr.lambda.Lambda;
import edu.uw.cs.lil.tiny.mr.lambda.Literal;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalConstant;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpressionRuntimeException;
import edu.uw.cs.lil.tiny.mr.lambda.Variable;

/**
 * Apply a functional expression to an argument. The result is simplified.
 * 
 * @author Luke Zettlemoyer
 */
public class ApplyAndSimplify extends AbstrcatSimplify {
	/**
	 * The argument of the application operation, e.g. x in f(x).
	 */
	private final LogicalExpression			appliedToArg;
	
	/**
	 * The variables of the argument {@link #appliedToArg}. We track them so we
	 * can make sure there's no overlap of variables between the two
	 * expressions. If such an overlap exists, we replace variables.
	 */
	private final Set<Variable>				argVars;
	
	/**
	 * Mapping of variables in the given logical expressions to new variables
	 * that don't overlap with the variables of the argument. This mapping
	 * contains only variables that were replaced.
	 */
	private final Map<Variable, Variable>	oldVariablesToNew	= new HashMap<Variable, Variable>();
	
	/**
	 * The variables that represents the argument in the given logical
	 * expression
	 */
	private final LogicalExpression			rootVariable;
	
	/**
	 * Variable for temporary storage of the result as we walk the structure.
	 */
	LogicalExpression						result				= null;
	
	/**
	 * This constructor is private as a small part of the logic is in the static
	 * {@link #of(LogicalExpression, LogicalExpression, boolean, boolean)}
	 * method. Therefore, this visitor is not independent.
	 * 
	 * @param appliedToArg
	 * @param rootVariable
	 * @param doSimplify
	 * @param doApplication
	 */
	private ApplyAndSimplify(LogicalExpression appliedToArg,
			Variable rootVariable) {
		this.appliedToArg = appliedToArg;
		this.rootVariable = rootVariable;
		this.argVars = GetVariables.of(appliedToArg);
	}
	
	public static LogicalExpression of(LogicalExpression exp,
			LogicalExpression arg) {
		// Verify type matching. The functor must be have a complex type, and
		// need to be in some kind of parent-child relationship with the
		// argument, as we allow flexible typing syntax-wise.
		if (!exp.getType().isComplex()
				|| !exp.getType().getDomain()
						.isExtendingOrExtendedBy(arg.getType())) {
			// Case typing mismatch
			return null;
		} else if (exp instanceof Lambda) {
			// Case the functor is a Lambda expression
			final Lambda lambda = (Lambda) exp;
			final Variable variable = lambda.getArgument();
			
			final ApplyAndSimplify visitor = new ApplyAndSimplify(arg, variable);
			
			visitor.visit(lambda.getBody());
			
			return visitor.tempReturn;
		} else if (exp instanceof Literal) {
			// Case the functor is a literal, append the argument to
			// the end of the arguments list
			return Simplify.of(literalApplication((Literal) exp, arg));
		} else if (exp instanceof Variable || exp instanceof LogicalConstant) {
			// Case the functor is a variable of logical constant,
			// create the a literal with the functor as predicate and the
			// argument as the only argument in the argument list
			return Simplify.of(termApplication(exp, arg));
		} else {
			// Should never happen
			throw new LogicalExpressionRuntimeException(
					"Impossible condition: un-handled logical expression object");
		}
	}
	
	private static LogicalExpression literalApplication(Literal literal,
			LogicalExpression arg) {
		final List<LogicalExpression> newArgs = new ArrayList<LogicalExpression>(
				literal.getArguments().size() + 1);
		newArgs.addAll(literal.getArguments());
		newArgs.add(arg);
		return new Literal(literal.getPredicate(), newArgs,
				LogicLanguageServices.getTypeComparator(),
				LogicLanguageServices.getTypeRepository());
	}
	
	private static LogicalExpression termApplication(LogicalExpression exp,
			LogicalExpression arg) {
		final List<LogicalExpression> arguments = new ArrayList<LogicalExpression>(
				1);
		arguments.add(arg);
		return new Literal(exp, arguments,
				LogicLanguageServices.getTypeComparator(),
				LogicLanguageServices.getTypeRepository());
	}
	
	@Override
	public void visit(Variable variable) {
		if (variable == rootVariable) {
			tempReturn = appliedToArg;
			return;
		}
		if (argVars.contains(variable)) {
			if (!oldVariablesToNew.containsKey(variable)) {
				oldVariablesToNew.put(variable,
						new Variable(variable.getType()));
			}
			tempReturn = oldVariablesToNew.get(variable);
		} else {
			tempReturn = variable;
		}
	}
	
}