package org.bverify.throughput;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bverify.bverify.BVerifyServerUtils;
import org.bverify.proofs.CategoricalQueryProof;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.SimpleRecord;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import edu.rice.historytree.ProofError;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ConcurrentProofGeneration {
	
	public static final int NUMBER_OF_RECORDS = 10000;
	public static final int NUMBER_OF_CATEGORICAL_ATTRIBUTES = 3;
	public static final int NUMBER_OF_NUMERICAL_ATTRIBUTES = 1;
	public static final int SEED = 10562620;
	public static final int COMMITMENT_INTERVAL = 100;
	public static final int NUM_COMMITMENTS_TO_VERIFY = 10;

	@State(Scope.Group)
	@Threads(10)
	public static class RequestProof {

		BVerifyServerUtils bverifyserver;
		
		// for the queries
		CategoricalAttributes filter;
		
		
		@Setup(Level.Trial)
		public void prepare() throws InsufficientMoneyException {
			
			// this is a bverfiy server that makes 'commitments' every
			// COMMITMENT_INTERVAL records but does not actually publish a commitment txn 
			// to the blockchain (this makes testing easier)
			this.bverifyserver = new BVerifyServerUtils(null, false, COMMITMENT_INTERVAL);
			
			// create a bunch of random records
			// we use a PRNG to make sure that we get the same (random) list 
			// for all tests to make sure things are consistent 
			List<SimpleRecord> records = SimpleRecord.simpleRecordFacotry(SEED, NUMBER_OF_RECORDS, 
					NUMBER_OF_NUMERICAL_ATTRIBUTES, NUMBER_OF_CATEGORICAL_ATTRIBUTES, new Date());
			
			for(SimpleRecord r: records) {
				this.bverifyserver.addRecord(r);
			}
			
			System.out.println("BVerify setup with "+this.bverifyserver.getTotalNumberOfRecords()+" records");
			
			this.filter = new CategoricalAttributes(NUMBER_OF_CATEGORICAL_ATTRIBUTES);
			this.filter.setAttribute(0, true);
		}
		

		public CategoricalQueryProof constructQueryProofReaderWriterLock() throws ProofError {
			// what b_verify uses
			return this.bverifyserver.queryRecordsByFilter(this.filter);
		}
		
	}
	
	@Benchmark
	@Group("query_proof_multiple_threads_reader_writer_lock")
	@GroupThreads(10)
	public CategoricalQueryProof query_proof_multiple_threads_reader_writer_lock(RequestProof state) throws ProofError {
		return state.constructQueryProofReaderWriterLock();
	}
	
	
}
