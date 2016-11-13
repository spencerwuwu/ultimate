package de.uni_freiburg.informatik.ultimate.deltadebugger.core.generators.hdd;

import de.uni_freiburg.informatik.ultimate.deltadebugger.core.IVariantGenerator;
import de.uni_freiburg.informatik.ultimate.deltadebugger.core.generators.hdd.changes.ChangeCollector;
import de.uni_freiburg.informatik.ultimate.deltadebugger.core.parser.pst.interfaces.IPSTNode;

/**
 * Strategy for the HDD inspired pass that controls if and how nodes of the tree are changed.
 */
@FunctionalInterface
public interface IHddStrategy {
	/**
	 * Allows to add changes that are not directly related to a child node, for instance individual tokens. In fact, the
	 * generated changes should not overlap any change generated for a child.
	 *
	 * @param node
	 *            PST node
	 * @param changeCollector
	 *            collector of changes
	 */
	default void createAdditionalChangesForExpandedNode(final IPSTNode node, final ChangeCollector changeCollector) {
		// no default behavior required
	}
	
	/**
	 * Create the change for node. If it can be applied, the subtree is not considered anymore. Otherwise it will be
	 * expanded, i.e. a change for each child will be created.
	 *
	 * @param node
	 *            PST node
	 * @param changeCollector
	 *            collector of changes
	 */
	void createChangeForNode(IPSTNode node, ChangeCollector changeCollector);
	
	/**
	 * Determines if the changes generated by the node will be tested with other changes on the same level or not.
	 * If true is returned for a node, all changes that are generated for the children of this node (not the node
	 * itself!) will have their own {@link IVariantGenerator} instance and will effectively be search on their own.<br>
	 * Example: Returning true for function definition bodies causes that each body compound statement is reduced
	 * individually instead of together with all other nodes on the same level. The search tree may become wider.
	 *
	 * @param node
	 *            PST node
	 * @return whether the changes created by expanding node will be returned searched individually
	 */
	default boolean expandIntoOwnGroup(final IPSTNode node) {
		return false;
	}
	
	/**
	 * Determines if a node without changes will be expanded immediately instead on the next level.
	 * The search tree depth may be reduced.
	 *
	 * @param node
	 *            PST node
	 * @return wether changes for children should take the place immediately if no changes are generated for node
	 */
	default boolean expandUnchangeableNodeImmediately(final IPSTNode node) {
		return false;
	}
	
	/**
	 * Determines if a subtree starting at node is processed or skipped.
	 *
	 * @param node
	 *            PST node
	 * @return whether the full subtree starting at node will be skipped
	 */
	default boolean skipSubTree(final IPSTNode node) {
		return false;
	}
}
