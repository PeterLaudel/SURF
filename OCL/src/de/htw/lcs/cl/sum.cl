// Falsches Ergebnis
//Running CLFloatMatrixPerformanceTest with 100 epochs
//	TwoStageSumPerformanceTest:
//	Mdim:  32	     17ms (avg:       0ms)
//	Mdim:  64	     12ms (avg:       0ms)
//	Mdim: 128	     12ms (avg:       0ms)
//	Mdim: 256	     10ms (avg:       0ms)
//	Mdim: 512	     12ms (avg:       0ms)
//	Mdim:1024	     17ms (avg:       0ms)
//	Mdim:2048	     34ms (avg:       0ms)
//	Mdim:4096	    100ms (avg:       1ms)


__kernel void two_stage_sum_kernel(
		__global float* buffer,
		__global float* result,
		__const int length,
		__local float* scratch)
{
  int global_index = get_global_id(0);
  float accumulator = INFINITY;
  // Loop sequentially over chunks of input vector
  while (global_index < length) {
    float element = buffer[global_index];
    accumulator = (accumulator < element) ? accumulator : element;
    global_index += get_global_size(0);
  }

  // Perform parallel reduction
  int local_index = get_local_id(0);
  scratch[local_index] = accumulator;
  barrier(CLK_LOCAL_MEM_FENCE);
  for(int offset = get_local_size(0) / 2;
      offset > 0;
      offset = offset / 2) {
    if (local_index < offset) {
      float other = scratch[local_index + offset];
      float mine = scratch[local_index];
      scratch[local_index] = (mine < other) ? mine : other;
    }
    barrier(CLK_LOCAL_MEM_FENCE);
  }
  if (local_index == 0) {
    result[get_group_id(0)] = scratch[0];
  }
}

// Falsches Ergebnis
//Running CLFloatMatrixPerformanceTest with 100 epochs
//	TwoStageSumPerformanceTest:
//	Mdim:  32	     17ms (avg:       0ms)
//	Mdim:  64	     13ms (avg:       0ms)
//	Mdim: 128	     12ms (avg:       0ms)
//	Mdim: 256	     10ms (avg:       0ms)
//	Mdim: 512	     10ms (avg:       0ms)
//	Mdim:1024	     12ms (avg:       0ms)
//	Mdim:2048	     11ms (avg:       0ms)
//  Mdim:4096	     14ms (avg:       0ms)
__kernel void two_stage_sum_kernel1(
		__global float* buffer,
		__global float* result,
		__const int length,
		__local float* scratch)
{

  int global_index = get_global_id(0);
  int local_index = get_local_id(0);
  // Load data into local memory
  if (global_index < length) {
    scratch[local_index] = buffer[global_index];
  } else {
    // Infinity is the identity element for the min operation
    scratch[local_index] = INFINITY;
  }
  barrier(CLK_LOCAL_MEM_FENCE);
  for(int offset = 1;
      offset < get_local_size(0);
      offset <<= 1) {
    int mask = (offset << 1) - 1;
    if ((local_index & mask) == 0) {
      float other = scratch[local_index + offset];
      float mine = scratch[local_index];
      scratch[local_index] = (mine < other) ? mine : other;
    }
    barrier(CLK_LOCAL_MEM_FENCE);
  }
  if (local_index == 0) {
    result[get_group_id(0)] = scratch[0];
  }
}


// http://stackoverflow.com/questions/10139314/double-sum-reduction-opencl-tutorial
__kernel void floatSum(
		__global float* inVector,
		__global float* outVector,
		const int inVectorSize,
		__local float* resultScratch)
{
    int gid = get_global_id(0);
    int wid = get_local_id(0);
    int wsize = get_local_size(0);
    int grid = get_group_id(0);
    int grcount = get_num_groups(0);

    int i;
    int workAmount = inVectorSize/grcount;
    int startOffset = workAmount * grid + wid;
    int maxOffset = workAmount * (grid + 1);
    maxOffset = select(maxOffset,inVectorSize,maxOffset > inVectorSize);

    resultScratch[wid] = 0.0;
    for(i=startOffset;i<maxOffset;i+=wsize){
            resultScratch[wid] += inVector[i];
    }
    barrier(CLK_LOCAL_MEM_FENCE);

    if(gid == 0){
            for(i=1;i<wsize;i++){
                    resultScratch[0] += resultScratch[i];
            }
            outVector[grid] = resultScratch[0];
    }
}


__kernel void reduceSum(
	__global const float* input,
	__global float* outSum,
	__const uint N,
	__local float* shared
	)
{
	const uint tid = get_local_id (0);

	if( get_global_id (0) < N)
		shared[tid] = input[ get_global_id (0) ];
	else // out of data, zero
		shared[tid] = 0;
	barrier( CLK_LOCAL_MEM_FENCE );

	for(uint s = get_local_size (0) /2; s > 0; s /= 2) {
		if(tid < s) {
		 	shared[tid] += shared[tid + s];
			//__local float* sA = &shared[tid ];
			//__local float* sB = &shared[tid + s];
			//sA += sB;
		}
		barrier( CLK_LOCAL_MEM_FENCE );
	}

	if(tid == 0)
		outSum[ get_group_id(0)] = shared[0]; // write the group result
}

