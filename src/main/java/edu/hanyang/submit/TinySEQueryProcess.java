package edu.hanyang.submit;

import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import edu.hanyang.indexer.DocumentCursor;
import edu.hanyang.indexer.PositionCursor;
import edu.hanyang.indexer.IntermediateList;
import edu.hanyang.indexer.IntermediatePositionalList;
import edu.hanyang.indexer.QueryPlanTree;
import edu.hanyang.indexer.QueryProcess;
import edu.hanyang.indexer.StatAPI;

public class TinySEQueryProcess implements QueryProcess {

	@Override
	public void op_and_w_pos(DocumentCursor op1, DocumentCursor op2, int shift, IntermediatePositionalList out)
			throws IOException {
		// TODO Auto-generated method stub
		while(!op1.is_eol() && !op2.is_eol()){
			if(op1.get_docid() < op2.get_docid()){
				op1.go_next();
			}else
			if(op1.get_docid() > op2.get_docid()){
				op2.go_next();
			}else{
				PositionCursor p1 = op1.get_position_cursor();
				PositionCursor p2 = op2.get_position_cursor();
				while(!p1.is_eol() && !p2.is_eol()){
					if(p1.get_pos() + shift < p2.get_pos()){
						p1.go_next();
					}else
					if(p1.get_pos() + shift > p2.get_pos()){
						p2.go_next();
					}else{
						out.put_docid_and_pos(op1.get_docid(), p1.get_pos());
						p1.go_next();
						p2.go_next();
					}
				}
				op1.go_next();
				op2.go_next();
			}
		}
	}
	
	@Override
	public void op_and_wo_pos(DocumentCursor op1, DocumentCursor op2, IntermediateList out) throws IOException {
		// TODO Auto-generated method stub
		while(!op1.is_eol() && !op2.is_eol()){
			if(op1.get_docid() < op2.get_docid()){
				op1.go_next();
			}else
			if(op1.get_docid() > op2.get_docid()){
				op2.go_next();
			}else{
				out.put_docid(op1.get_docid());
				op1.go_next();
				op2.go_next();
			}
		}
	}

	@Override
	public QueryPlanTree parse_query(String query, StatAPI stat) throws Exception {
		QueryPlanTree qp = new QueryPlanTree();
		QueryPlanTree.QueryPlanNode node;
		Queue<QueryPlanTree.QueryPlanNode> queue = new LinkedList<>();

//		String query = "\"3 4\" 7";
		String[] result = query.split(" ");
		int inShift = 0;
		for(String r: result){
			if(r.length() == 0){
				//pass
			}else
			if(r.contains("\"") && r.length() == 1){
				inShift = inShift == 1 ? 0 : 1;
			}else
			if(r.contains("\"")){
				if(r.charAt(0) == '\"'){
					r = r.substring(1);
					inShift = 1;
				}else{
					r = r.substring(0,1);
					inShift = 0;
				}
				node = qp.new QueryPlanNode();
				node.type = QueryPlanTree.NODE_TYPE.OPRAND;
				node.shift = 1;
				node.termid = Integer.valueOf(r);
				queue.add(node);
			}else{
				node = qp.new QueryPlanNode();
				node.type = QueryPlanTree.NODE_TYPE.OPRAND;
				node.shift = inShift;
				node.termid = Integer.valueOf(r);
				queue.add(node);
			}
		}

		QueryPlanTree.QueryPlanNode n1, n2;
		while(queue.size() > 1){
			n1 = queue.poll();
			if(queue.size() != 1 && (n1.shift == 0 && queue.peek().shift > 0) || (n1.shift > 0 && queue.peek().shift == 0)){
				queue.add(n1);
				continue;
			}
			n2 = queue.poll();
			if(n1.shift != 0 && n2.shift != 0){
				node = qp.new QueryPlanNode();
				node.type = QueryPlanTree.NODE_TYPE.OP_SHIFTED_AND;
//				node.shift = Math.abs(n2.shift - n1.shift);
				node.shift = n1.type == QueryPlanTree.NODE_TYPE.OPRAND  && n2.type == QueryPlanTree.NODE_TYPE.OPRAND ? 1 : n1.shift + n2.shift;
				node.left = n1;
				node.right = n2;
			}else{
				if(n1.type == QueryPlanTree.NODE_TYPE.OPRAND || n1.shift != 0){
					QueryPlanTree.QueryPlanNode tempNode = qp.new QueryPlanNode();
					tempNode.type = QueryPlanTree.NODE_TYPE.OP_REMOVE_POS;
					tempNode.shift = 0;
					tempNode.left = n1;
					n1 = tempNode;
				}
				if(n2.type == QueryPlanTree.NODE_TYPE.OPRAND || n2.shift != 0){
					QueryPlanTree.QueryPlanNode tempNode = qp.new QueryPlanNode();
					tempNode.type = QueryPlanTree.NODE_TYPE.OP_REMOVE_POS;
					tempNode.shift = 0;
					tempNode.left = n2;
					n2 = tempNode;
				}
				node = qp.new QueryPlanNode();
				node.type = QueryPlanTree.NODE_TYPE.OP_AND;
				node.shift = 0;
				node.left = n1;
				node.right = n2;
			}
			queue.add(node);
		}

		node = queue.poll();
		if(node.type == QueryPlanTree.NODE_TYPE.OPRAND){
			QueryPlanTree.QueryPlanNode root = qp.new QueryPlanNode();
			root.type = QueryPlanTree.NODE_TYPE.OP_REMOVE_POS;
			root.left = node;
			qp.root = root;
		}else{
			qp.root = node;
		}
		return qp;
	}

	public static void main(String[] args){

	}
}
