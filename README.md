# Stable Bloom Filter

An implementation of a Stable Bloom Filter for filtering duplicates out of data streams as described in [Approximately Detecting Duplicates for Streaming Data
using Stable Bloom Filters](https://webdocs.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf)

## Bloom Filter

A Bloom filter is a space-efficient probabilistic data structure that is used to test whether an element is a member of a set. False positive matches are possible, but false negatives are not. In other words, a query returns either “possibly in set” or “definitely not in set”. Elements can be added to the set, but not removed.

## Stable Bloom Filter

A Stable Bloom Filter (SBF) is designed to continuously evict stale information in order to make room for more recently added elements. The false-positive rate of a classic Bloom filter will eventually reach 1, at which point all queries will result in a false positive. The stable-point property of an SBF, however, means that the false-positive rate asymptotically approaches a configurable fixed constant. Stable Bloom Filters are particularly beneficial for cases where the size of the data set is unknown and memory is limited. For example, an SBF can be used to deduplicate events from an unbounded event stream with a specified upper bound on false positives and minimal false negatives.

## Usage

```scala
  import CanGenerateHashFrom._

  val sbf = StableBloomFilter[String](
    numberOfCells = 1000,
    cellSize = 3,
    falsePositiveRate = 0.01
  )

  val itemA = "item_a"
  sbf.put(itemA)
  sbf.mightContain(itemA) shouldBe true
```

## References
[Approximately Detecting Duplicates for Streaming Data
using Stable Bloom Filters](https://webdocs.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf)